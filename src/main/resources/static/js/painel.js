(function () {
    'use strict';

    const formulario = document.getElementById('painel-filtros');
    if (!formulario) {
        return;
    }

    const elementos = {
        inicio: document.getElementById('painel-inicio'),
        fim: document.getElementById('painel-fim'),
        status: document.getElementById('painel-status'),
        produto: document.getElementById('painel-produto'),
        atualizar: document.getElementById('painel-atualizar'),
        limpar: document.getElementById('painel-limpar'),
        erro: document.getElementById('painel-erro'),
        atualizacao: document.getElementById('painel-atualizacao')
    };

    const moeda = new Intl.NumberFormat('pt-BR', {style: 'currency', currency: 'BRL'});
    const inteiro = new Intl.NumberFormat('pt-BR');
    const graficos = {};

    function dataLocal(data) {
        return [data.getFullYear(), String(data.getMonth() + 1).padStart(2, '0'),
            String(data.getDate()).padStart(2, '0')].join('-');
    }

    function datasPadrao() {
        const hoje = new Date();
        const inicio = new Date(hoje);
        inicio.setDate(inicio.getDate() - 29);
        elementos.inicio.value = dataLocal(inicio);
        elementos.fim.value = dataLocal(hoje);
    }

    function mostrarErro(mensagem) {
        elementos.erro.textContent = mensagem;
        elementos.erro.classList.remove('d-none');
    }

    function limparErro() {
        elementos.erro.textContent = '';
        elementos.erro.classList.add('d-none');
    }

    function parametros() {
        const params = new URLSearchParams();
        for (const nome of ['inicio', 'fim', 'status']) {
            if (elementos[nome].value) {
                params.set(nome, elementos[nome].value);
            }
        }
        if (elementos.produto.value) {
            params.set('produtoId', elementos.produto.value);
        }
        return params.toString();
    }

    async function consultar(endpoint, query = '') {
        const url = '/api/painel/' + endpoint + (query ? '?' + query : '');
        const resposta = await fetch(url, {headers: {'Accept': 'application/json'}, cache: 'no-store'});
        if (!resposta.ok) {
            let mensagem = 'Não foi possível consultar os dados (' + resposta.status + ').';
            try {
                const erro = await resposta.json();
                mensagem = erro.erro || mensagem;
            } catch (ignored) {
                // A resposta de erro pode não ter conteúdo JSON.
            }
            throw new Error(mensagem);
        }
        return resposta.json();
    }

    function substituirGrafico(nome, canvasId, tipo, dados, opcoes) {
        if (graficos[nome]) {
            graficos[nome].destroy();
        }
        const ctx = document.getElementById(canvasId).getContext('2d');
        graficos[nome] = new Chart(ctx, {type: tipo, data: dados, options: opcoes});
    }

    function graficoHistorico(historico) {
        substituirGrafico('historico', 'painel-grafico-historico', 'line', {
            labels: historico.map(l => l.data.split('-').reverse().slice(0, 2).join('/')),
            datasets: [{
                label: 'Vendas (R$)',
                data: historico.map(l => Number(l.total)),
                borderColor: '#227a96',
                backgroundColor: 'rgba(34,122,150,0.10)',
                pointRadius: historico.length > 60 ? 0 : 3,
                fill: true,
                lineTension: 0.15
            }]
        }, {
            responsive: true,
            maintainAspectRatio: false,
            legend: {display: false},
            tooltips: {callbacks: {label: item => moeda.format(item.yLabel)}},
            scales: {yAxes: [{ticks: {beginAtZero: true, callback: valor => moeda.format(valor)}}]}
        });
    }

    function graficoSituacao(situacao) {
        const presentes = situacao.filter(s => Number(s.quantidade) > 0);
        document.getElementById('painel-situacao-vazia').classList.toggle('d-none', presentes.length > 0);
        substituirGrafico('situacao', 'painel-grafico-situacao', 'pie', {
            labels: presentes.map(s => s.descricao),
            datasets: [{
                data: presentes.map(s => Number(s.quantidade)),
                backgroundColor: ['#2563eb', '#f59e0b', '#8b5cf6', '#0ea5e9', '#14b8a6', '#16a34a', '#dc2626']
            }]
        }, {
            responsive: true,
            maintainAspectRatio: false,
            legend: {position: 'bottom'}
        });
    }

    function graficoPopulares(populares) {
        document.getElementById('painel-populares-vazio').classList.toggle('d-none', populares.length > 0);
        substituirGrafico('populares', 'painel-grafico-populares', 'bar', {
            labels: populares.map(p => p.nome),
            datasets: [{
                label: 'Unidades vendidas',
                data: populares.map(p => Number(p.quantidade)),
                backgroundColor: '#0d9488',
                maxBarThickness: 56
            }]
        }, {
            responsive: true,
            maintainAspectRatio: false,
            legend: {display: false},
            scales: {yAxes: [{ticks: {beginAtZero: true, precision: 0}}]}
        });
    }

    function limparResultados() {
        for (const id of ['painel-faturamento', 'painel-finalizados', 'painel-ticket', 'painel-cancelamentos']) {
            document.getElementById(id).textContent = '—';
        }
        elementos.atualizacao.textContent = '';
        for (const nome in graficos) {
            graficos[nome].destroy();
            delete graficos[nome];
        }
    }

    async function atualizar() {
        limparErro();
        elementos.atualizar.disabled = true;
        elementos.atualizar.textContent = 'Carregando...';
        try {
            const query = parametros();
            const [resumo, historico, situacao, populares] = await Promise.all([
                consultar('resumo', query),
                consultar('historico', query),
                consultar('situacao', query),
                consultar('populares', query)
            ]);
            document.getElementById('painel-faturamento').textContent = moeda.format(resumo.faturamento);
            document.getElementById('painel-finalizados').textContent = inteiro.format(resumo.pedidosFinalizados);
            document.getElementById('painel-ticket').textContent = moeda.format(resumo.ticketMedio);
            document.getElementById('painel-cancelamentos').textContent = inteiro.format(resumo.cancelamentos);
            graficoHistorico(historico);
            graficoSituacao(situacao);
            graficoPopulares(populares);
            elementos.atualizacao.textContent = 'Atualizado às ' + new Date().toLocaleTimeString('pt-BR');
        } catch (erro) {
            limparResultados();
            mostrarErro(erro.message || 'Erro ao carregar os dados.');
        } finally {
            elementos.atualizar.disabled = false;
            elementos.atualizar.textContent = 'Atualizar';
        }
    }

    async function carregarProdutos() {
        try {
            const produtos = await consultar('produtos');
            for (const produto of produtos) {
                const opcao = document.createElement('option');
                opcao.value = produto.id;
                opcao.textContent = produto.nome;
                elementos.produto.appendChild(opcao);
            }
        } catch (erro) {
            mostrarErro('Não foi possível carregar os produtos: ' + erro.message);
        }
    }

    formulario.addEventListener('submit', function (evento) {
        evento.preventDefault();
        atualizar();
    });
    elementos.limpar.addEventListener('click', function () {
        formulario.reset();
        datasPadrao();
        atualizar();
    });

    datasPadrao();
    carregarProdutos().then(atualizar);
})();
