//na pagina cadastro, esconde as opções de doador e ong após fazer a escolha
function toggleForm(type) {
  document.getElementById("tipo-cadastro").classList.add("hidden"); 

  document.getElementById("cadastro-doador").classList.add("hidden");
  document.getElementById("cadastro-ong").classList.add("hidden");

  if (type === "doador") {
    document.getElementById("cadastro-doador").classList.remove("hidden");
  } else {
    document.getElementById("cadastro-ong").classList.remove("hidden");
  }
}

//script para cadastrar Doador
async function cadastrarDoador(event) {
  event.preventDefault();

  const nome = document.getElementById("nomeDoador").value.trim();
  const cpf = document.getElementById("cpfDoador").value.trim();
  const senha = document.getElementById("senhaDoador").value;
  const confirmar = document.getElementById("confSenhaDoador").value;
  const cep = document.getElementById("cepDoador").value.trim();
  const endereco = document.getElementById("enderecoDoador").value.trim();
  const dataNascimento = document.getElementById("anoDoador").value;

  if (senha !== confirmar) {
    document.getElementById("cadastro-error").textContent = "As senhas não coincidem.";
    return;
  }

  try {
    const response = await fetch("http://localhost:8080/auth/register/doador", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        login: cpf,
        senha: senha,
        nome: nome,
        cep: cep,
        endereco: endereco,
        dataNascimento: dataNascimento  // ✅ inclui no payload!
      })
    });

    const data = await response.json();

    if (response.ok && data.message) {
      alert("Cadastro de doador realizado com sucesso!");
      document.getElementById("cadastro-doador").reset();  // ✅ opcional: limpa o form
      document.getElementById("cadastro-error").textContent = "";  // limpa erros
    } else {
      document.getElementById("cadastro-error").textContent = data.error || "Erro no cadastro.";
    }
  } catch (error) {
    console.error("Erro:", error);
    document.getElementById("cadastro-error").textContent = "Erro na conexão com o servidor.";
  }
}

//script para cadastrar Ong
async function cadastrarOng(event) {
  event.preventDefault();

  const cep = document.getElementById("cepOng").value.trim();
  const endereco = document.getElementById("enderecoOng").value.trim();

  const formData = new FormData();
  formData.append("nome", document.getElementById("nomeOng").value.trim());
  formData.append("login", document.getElementById("cnpjOng").value.trim());
  formData.append("senha", document.getElementById("senhaOng").value);
  formData.append("cep", cep);
  formData.append("endereco", endereco);

  const imagem = document.getElementById("imagemOng").files[0];
  if (imagem) formData.append("imagem", imagem);

  const senha = document.getElementById("senhaOng").value;
  const confirmar = document.getElementById("confSenhaOng").value;
  if (senha !== confirmar) {
    document.getElementById("cadastro-error").textContent = "As senhas não coincidem.";
    return;
  }

  try {
    const response = await fetch("http://localhost:8080/auth/register/ong", {
      method: "POST",
      body: formData
    });

    const data = await response.json();
    if (response.ok && data.message) {
      alert("Cadastro de ONG realizado com sucesso!");
    } else {
      document.getElementById("cadastro-error").textContent = data.error || "Erro no cadastro.";
    }
  } catch (error) {
    console.error("Erro:", error);
    document.getElementById("cadastro-error").textContent = "Erro na conexão com o servidor.";
  }
}

//script para colocar o endereço automaticamente com o CEP
async function buscarEndereco(cep, campoEndereco, campoNumero) {
  if (cep.length !== 8) return;

  try {
    const response = await fetch(`https://viacep.com.br/ws/${cep}/json/`);
    const data = await response.json();
    if (!data.erro) {
      const numero = document.getElementById(campoNumero).value.trim();
      const enderecoFormatado = `${data.logradouro}, ${numero} - ${data.bairro}, ${data.localidade} - ${data.uf}`;
      document.getElementById(campoEndereco).value = enderecoFormatado;
    } else {
      document.getElementById(campoEndereco).value = "CEP não encontrado";
    }
  } catch (error) {
    console.error("Erro ao buscar endereço:", error);
  }
}

//script do index
async function login(event) {
  event.preventDefault();

  const user = document.getElementById("login-user").value.trim();
  const pass = document.getElementById("login-pass").value;

  const params = new URLSearchParams();
  params.append("login", user);
  params.append("senha", pass);

  try {
    const response = await fetch("http://localhost:8080/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: params.toString()
    });

    const data = await response.json();

    if (response.ok && data.message) {
      if (data.tipo === "DOADOR") {
        location.href = "doador";
      } else if (data.tipo === "ONG") {
        location.href = "ong";
      } else {
        alert("Tipo de usuário desconhecido.");
      }
    } else {
      alert(data.error || "Usuário ou senha inválidos.");
    }
  } catch (error) {
    console.error("Erro:", error);
    document.getElementById("login-error").textContent = "Erro na conexão com o servidor.";
  }
}

//espera a pagina carregar, procura o loginForm e se ele existe conecta com a função login
document.addEventListener("DOMContentLoaded", () => {
  const loginForm = document.getElementById("loginForm");
  if (loginForm) {
    loginForm.addEventListener("submit", login);
  }
});

//logout
function logout() {
  fetch('/auth/logout', {
    method: 'POST',
    credentials: 'include'  // importante para enviar cookies/sessão
  })
  .then(response => {
    if (response.ok) {
      window.location.href = '/';  // redireciona depois do logout
    } else {
      alert('Erro ao fazer logout');
    }
  })
  .catch(err => {
    alert('Erro ao fazer logout');
    console.error(err);
  });
}

//verifica se existe o botao sair se existir define a função de logout para ele
document.addEventListener('DOMContentLoaded', () => {
  const btnLogout = document.querySelector("button.botao-secundario[onclick='logout()']");
  if (btnLogout) {
    btnLogout.onclick = logout;
  }
});

//script da pagina doador_historico
async function carregarHistorico() {
  try {
    const res = await fetch('/doacoes/doador/historico');
    if (!res.ok) {
      throw new Error('Não foi possível carregar o histórico.');
    }

    const doacoes = await res.json();
    const container = document.getElementById('historicoDoacoes');
    if (!container) return;

    container.innerHTML = '';

    if (doacoes.length === 0) {
      container.innerHTML = '<p>Você ainda não realizou nenhuma doação.</p>';
      return;
    }

    doacoes.forEach(doacao => {
      const div = document.createElement('div');
      div.className = 'doacao';

      const spanStatus = document.createElement("span");
      spanStatus.textContent = doacao.status;

      if (doacao.status === "NEGADO" && doacao.motivoRecusa) {
        spanStatus.title = "Motivo: " + doacao.motivoRecusa;
        spanStatus.style.color = "red";
        spanStatus.style.cursor = "help";
      }

      div.innerHTML = `
        <p><strong>Descrição:</strong> ${doacao.descricao}</p>
        <p><strong>ONG:</strong> ${doacao.nomeOng}</p>
        <p><strong>Status:</strong> </p>
        <p><strong>Data:</strong> ${new Date(doacao.data).toLocaleString('pt-BR')}</p>
        <hr/>
      `;

      div.querySelector("p:nth-of-type(3)").appendChild(spanStatus);

      container.appendChild(div);
    });
  } catch (error) {
    console.error('Erro ao carregar histórico:', error);
    const container = document.getElementById('historicoDoacoes');
    if(container) container.innerHTML = '<p>Erro ao carregar histórico.</p>';
  }
}

//verifica se existe o historicoDoacoes se existir "liga" a função carregarHistorico (doador_historico)
document.addEventListener('DOMContentLoaded', () => {
  if (document.getElementById('historicoDoacoes')) {
    carregarHistorico();
  }
});

//script da pagina doador_ongs
async function carregarOngs() {
  try {
    const res = await fetch('/ongs');
    if (!res.ok) throw new Error('Falha ao obter ONGs');
    const ongs = await res.json();
    const container = document.getElementById('listaOngs');
    if (!container) return;

    container.innerHTML = '';

    if (ongs.length === 0) {
      container.innerHTML = '<p>Nenhuma ONG cadastrada.</p>';
      return;
    }

    ongs.forEach(ong => {
      const div = document.createElement('div');

      if (ong.imagem) {
        const img = document.createElement('img');
        img.classList.add('ong-img');
        img.alt = `Imagem da ONG ${ong.nome}`;
        img.src = `data:image/jpeg;base64,${ong.imagem}`;
        div.appendChild(img);
      }

      const infoDiv = document.createElement('div');
      infoDiv.classList.add('ong-info');
//Aqui está mandando os dados da ong para a URL
      const nomeLink = document.createElement('a');
      nomeLink.textContent = ong.nome;
      nomeLink.classList.add('ong-link');
      nomeLink.href = `detalhes?idOng=${encodeURIComponent(ong.id)}`;


      infoDiv.appendChild(nomeLink);
      infoDiv.innerHTML += `<br>${ong.endereco}`;

      div.appendChild(infoDiv);

      container.appendChild(div);
    });
  } catch (error) {
    console.error('Erro ao carregar dados:', error);
    const container = document.getElementById('listaOngs');
    if (container) container.innerHTML = '<p>Erro ao carregar dados.</p>';
  }
}

//verifica se existe a listaOngs se existir "liga" a função carregarOngs (doador_ongs)
document.addEventListener('DOMContentLoaded', () => {
  if (document.getElementById('listaOngs')) {
    carregarOngs();
  }
});

//variável para armazenar dados do endereço da ONG
let enderecoBase = {
  logradouro: '',
  bairro: '',
  localidade: '',
  uf: ''
};

//script para atualizar endereço da ONG
function atualizarEnderecoCompleto() {
  const numero = document.getElementById('numero').value.trim();
  const enderecoInput = document.getElementById('endereco');
  if (enderecoBase.logradouro) {
    enderecoInput.value = numero
      ? `${enderecoBase.logradouro}, ${numero}, ${enderecoBase.bairro} - ${enderecoBase.localidade} / ${enderecoBase.uf}`
      : `${enderecoBase.logradouro}, ${enderecoBase.bairro} - ${enderecoBase.localidade} / ${enderecoBase.uf}`;
  } else {
    enderecoInput.value = '';
  }
}

//script para colocar o endereço automaticamente com o CEP só q na parte de atualizar ONG
async function buscarEnderecoPorCep(cep) {
  try {
    const res = await fetch(`https://viacep.com.br/ws/${cep}/json/`);
    const dados = await res.json();
    if (!dados.erro) {
      enderecoBase = {
        logradouro: dados.logradouro || '',
        bairro: dados.bairro || '',
        localidade: dados.localidade || '',
        uf: dados.uf || ''
      };
      atualizarEnderecoCompleto();
    } else {
      alert('CEP não encontrado.');
      enderecoBase = { logradouro: '', bairro: '', localidade: '', uf: '' };
      atualizarEnderecoCompleto();
    }
  } catch (error) {
    console.error('Erro ao buscar endereço:', error);
    enderecoBase = { logradouro: '', bairro: '', localidade: '', uf: '' };
    atualizarEnderecoCompleto();
  }
}

//script para colocar os dados da ong na pagina ong_dados
async function carregarDadosOng() {
  try {
    const res = await fetch('/auth/me', { credentials: 'include' });
    if (!res.ok) throw new Error('Não autenticado');
    const ong = await res.json();

    document.getElementById('cep').value = ong.cep || '';
    document.getElementById('numero').value = ong.numero || '';

    if (ong.endereco) {
      const enderecoParts = ong.endereco.split(',');
      enderecoBase.logradouro = enderecoParts[0] ? enderecoParts[0].trim() : '';
      const resto = ong.endereco.substring(enderecoBase.logradouro.length + 1).trim();
      const bairroLocalUf = resto.split(' - ');
      enderecoBase.bairro = bairroLocalUf[0] ? bairroLocalUf[0].replace(/^\d+,?/, '').trim() : '';
      const cidadeUf = bairroLocalUf[1] ? bairroLocalUf[1].split('/') : [];
      enderecoBase.localidade = cidadeUf[0] ? cidadeUf[0].trim() : '';
      enderecoBase.uf = cidadeUf[1] ? cidadeUf[1].trim() : '';
      
      atualizarEnderecoCompleto();
    }

    if (ong.imagem) {
      const preview = document.getElementById('preview');
      preview.src = `data:image/jpeg;base64,${ong.imagem}`;
      preview.style.display = 'block';
    }
  } catch (error) {
    console.error('Erro ao carregar dados da ONG:', error);
  }
}

//script para atualizar os dados da ong na pagina ong_dados
function initOngDados() {
  const cepInput = document.getElementById('cep');
  const numeroInput = document.getElementById('numero');
  const imagemInput = document.getElementById('imagem');
  const preview = document.getElementById('preview');
  const formOng = document.getElementById('form-ong');

  if (cepInput) {
    cepInput.addEventListener('blur', () => {
      const cep = cepInput.value.replace(/\D/g, '');
      if (cep.length === 8) {
        buscarEnderecoPorCep(cep);
      } else {
        enderecoBase = { logradouro: '', bairro: '', localidade: '', uf: '' };
        atualizarEnderecoCompleto();
      }
    });
  }

  if (numeroInput) {
    numeroInput.addEventListener('input', () => {
      atualizarEnderecoCompleto();
    });
  }

  if (imagemInput && preview) {
    imagemInput.addEventListener('change', (event) => {
      const input = event.target;
      if (input.files && input.files[0]) {
        const reader = new FileReader();
        reader.onload = function (e) {
          preview.src = e.target.result;
          preview.style.display = 'block';
        };
        reader.readAsDataURL(input.files[0]);
      }
    });
  }

  if (formOng) {
    formOng.addEventListener('submit', async (event) => {
      event.preventDefault();

      const formData = new FormData(formOng);

      try {
        const response = await fetch('/auth/atualizar-ong', {
          method: 'PUT',
          body: formData,
          credentials: 'include'
        });

        if (response.ok) {
          alert('Dados atualizados com sucesso!');
        } else {
          const errorText = await response.text();
          alert('Erro ao atualizar dados: ' + errorText);
        }
      } catch (error) {
        console.error('Erro ao enviar formulário:', error);
      }
    });
  }

  carregarDadosOng();
}

//verifica se existe o form-ong se existir "liga" a função initOngDados (ong_dados)
document.addEventListener('DOMContentLoaded', () => {
  if (document.getElementById('form-ong')) {
    initOngDados();
  }
});

//script da pagina ong_historico
async function carregarHistoricoDoacoes() {
  try {
    const response = await fetch("http://localhost:8080/doacoes/historico", { credentials: 'include' });
    const doacoes = await response.json();

    const lista = document.getElementById("lista-doacoes");
    const mensagemVazio = document.getElementById("mensagem-vazio");
    lista.innerHTML = "";

    if (doacoes.length === 0) {
      mensagemVazio.style.display = "block";
      return;
    } else {
      mensagemVazio.style.display = "none";
    }

    doacoes.forEach(doacao => {
      const li = document.createElement("li");

      const dataObj = new Date(doacao.data);
      const dataFormatada = dataObj.toLocaleDateString("pt-BR");
      const horaFormatada = dataObj.toLocaleTimeString("pt-BR", {
        hour: '2-digit',
        minute: '2-digit'
      });

      const texto = document.createTextNode(`Doação de ${doacao.nomeDoador} em ${dataFormatada} às ${horaFormatada} - Status: `);
      li.appendChild(texto);

      const spanStatus = document.createElement("span");
      spanStatus.textContent = doacao.status;

      if (doacao.status === "NEGADO" && doacao.motivoRecusa) {
        spanStatus.title = "Motivo: " + doacao.motivoRecusa;
        spanStatus.style.color = "red";
        spanStatus.style.cursor = "help";
      }

      li.appendChild(spanStatus);
      lista.appendChild(li);
    });

  } catch (error) {
    console.error("Erro ao carregar histórico de doações:", error);
  }
}

//verifica se existe os dados da lista-doacoes se existir "liga" a função carregarHistoricoDoacoes (ong_historico)
function initHistoricoDoacoes() {
  if (!document.getElementById("lista-doacoes")) return;
  carregarHistoricoDoacoes();
}

//verifica se existe a lista-doacoes se existir "liga" a função initHistoricoDoacoes (ong_historico)
document.addEventListener('DOMContentLoaded', () => {
  if (document.getElementById("lista-doacoes")) {
    initHistoricoDoacoes();
  }
});

let doacaoRecusadaId = null;

//script da pagina ong_validar
async function carregarDoacoesPendentes() {
  try {
    const response = await fetch("http://localhost:8080/doacoes/pendentes", { credentials: 'include' });
    const doacoes = await response.json();

    const container = document.getElementById("doacoes-container");
    container.innerHTML = "";

    if (doacoes.length === 0) {
      const msg = document.createElement("p");
      msg.textContent = "Nenhuma doação pendente.";
      container.appendChild(msg);
      return;
    }

    doacoes.forEach(doacao => {
      const p = document.createElement("p");

      const dataObj = new Date(doacao.data);
      const dataFormatada = dataObj.toLocaleDateString("pt-BR");
      const horaFormatada = dataObj.toLocaleTimeString("pt-BR", {
        hour: '2-digit',
        minute: '2-digit'
      });

      p.textContent = `Doação de ${doacao.nomeDoador} em ${dataFormatada} às ${horaFormatada} - ${doacao.descricao}`;

      const btnAceitar = document.createElement("button");
      btnAceitar.textContent = "Aceitar";
      btnAceitar.onclick = () => atualizarStatusDoacao(doacao.id, "ACEITO");

      const btnRecusar = document.createElement("button");
      btnRecusar.textContent = "Rejeitar";
      btnRecusar.onclick = () => abrirModalRecusa(doacao.id);

      p.appendChild(document.createTextNode(" "));
      p.appendChild(btnAceitar);
      p.appendChild(document.createTextNode(" "));
      p.appendChild(btnRecusar);

      container.appendChild(p);
    });
  } catch (error) {
    console.error("Erro ao carregar doações pendentes:", error);
  }
}

//script do status da doação
async function atualizarStatusDoacao(id, status, motivo = "") {
  try {
    await fetch(`http://localhost:8080/doacoes/${id}/status`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      credentials: 'include',
      body: JSON.stringify({ status, motivoRecusa: motivo })
    });
    carregarDoacoesPendentes();
  } catch (error) {
    console.error("Erro ao atualizar status da doação:", error);
  }
}

//script do status da doação
function abrirModalRecusa(id) {
  doacaoRecusadaId = id;
  document.getElementById("motivo-recusa-text").value = "";
  document.getElementById("modal-recusa").style.display = "flex";
}

//script do status da doação
function fecharModalRecusa() {
  doacaoRecusadaId = null;
  document.getElementById("modal-recusa").style.display = "none";
}

//script do status da doação
function confirmarRecusa() {
  const motivo = document.getElementById("motivo-recusa-text").value.trim();
  if (!motivo) {
    alert("Informe um motivo para recusar.");
    return;
  }
  atualizarStatusDoacao(doacaoRecusadaId, "NEGADO", motivo);
  fecharModalRecusa();
}

//script do status da doação
function initValidarDoacoes() {
  if (!document.getElementById("doacoes-container")) return;
  carregarDoacoesPendentes();

  // Associar eventos do modal
  document.querySelector("#modal-recusa button[onclick='confirmarRecusa()']").onclick = confirmarRecusa;
  document.querySelector("#modal-recusa button[onclick='fecharModal()']").onclick = fecharModalRecusa;
}

//verifica se existe doacoes-container se existir "liga" a initValidarDoacoes (ong_validar)
document.addEventListener('DOMContentLoaded', () => {
  if (document.getElementById("doacoes-container")) {
    initValidarDoacoes();
  }
});





