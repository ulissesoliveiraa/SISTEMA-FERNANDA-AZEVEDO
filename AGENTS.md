# AGENTS.md — Protótipo de Alta Fidelidade | Fernanda Azevedo

## 0. REGRA SUPREMA PARA O CODEX

Este arquivo é a especificação principal do protótipo de alta fidelidade da plataforma de telemedicina **Fernanda Azevedo**. O Codex deve implementar o protótipo completo, navegável, coerente e demonstrável de ponta a ponta.

Não entregar apenas layouts estáticos. Todos os botões, filtros, formulários, modais, menus, estados, permissões, históricos, chats, tickets, uploads, fluxos de consulta e ações administrativas descritos aqui devem possuir comportamento funcional no protótipo, mesmo que determinados serviços externos sejam simulados.

Quando uma funcionalidade depender de serviços de produção — videoconferência real, gateway de pagamento, antivírus/antimalware, e-mail transacional, assinatura digital, armazenamento em nuvem, autenticação Google real, notificações push ou IA externa — criar a interface, os estados, a lógica de domínio e um adaptador/mock claramente substituível posteriormente por integração real.

Nunca fingir que uma proteção simulada é segurança real. Na interface de demonstração ela pode aparecer funcionando, mas o código deve deixar claro onde entrará a integração de produção.

---

# 1. OBJETIVO DO PRODUTO

**Fernanda Azevedo** será uma plataforma digital de telemedicina acessível, humana e simples. O paciente deve conseguir criar conta, pagar por uma consulta, responder uma pré-anamnese curta, aguardar atendimento, realizar teleconsulta com médico por vídeo, conversar por chat, trocar arquivos, receber documentos médicos e consultar posteriormente todo o histórico do atendimento.

A plataforma também terá uma central de suporte completa com chatbot inicial, abertura automática de ticket quando necessário, chat humano em tempo real, anexos, imagens, áudios, histórico e envio de transcrição por e-mail.

O protótipo deve transmitir a sensação de um produto real pronto para demonstração a médicos, pacientes, parceiros e possíveis investidores.

---

# 2. TECNOLOGIAS OBRIGATÓRIAS DO PROTÓTIPO

## 2.1 Front-end

Usar:

- HTML5 semântico;
- CSS3;
- JavaScript ES6+;
- JavaScript modular;
- Fetch API para comunicação com o backend Java;
- Web APIs do navegador quando apropriado;
- MediaDevices API para teste/preview de câmera e microfone;
- Web Audio API somente quando útil para gravação/reprodução de áudio no protótipo.

Não usar React, Vue, Angular ou frameworks front-end pesados nesta fase.

## 2.2 Backend demonstrativo em Java

Usar Java com **Spring Boot** para tornar o protótipo mais fiel e permitir:

- endpoints REST;
- login e sessão demonstrativos;
- usuários e permissões;
- consultas;
- tickets;
- chats;
- uploads simulados;
- consentimentos;
- configurações do sistema;
- preço de consulta em tempo real;
- campanhas/banners do site;
- logs de auditoria;
- dados persistidos localmente durante a demonstração.

Pode usar H2 em modo arquivo para o protótipo. Organizar o código para futura migração a PostgreSQL.

## 2.3 O que NÃO implementar como produção nesta etapa

Não é necessário nesta fase:

- pagamento financeiro real;
- split financeiro real;
- teleconsulta P2P real entre usuários remotos;
- gravação real das consultas;
- armazenamento cloud real;
- antivírus real;
- sandbox real de arquivos;
- assinatura digital ICP-Brasil real;
- SMTP real obrigatório;
- prontuário homologado para produção.

Porém todos esses pontos devem ter telas, estados, serviços abstratos e mocks coerentes para facilitar a troca futura por integrações reais.

---

# 3. IDENTIDADE DO SISTEMA

Nome: **Fernanda Azevedo**

Assinatura sugerida: **Saúde perto de você.**

Tom da marca:

- próximo;
- acolhedor;
- simples;
- profissional;
- seguro;
- acessível;
- humano;
- moderno sem parecer elitizado.

Nunca usar linguagem de luxo, exclusividade ou “medicina premium”.

---

# 4. IDENTIDADE VISUAL

## 4.1 Paleta

Criar variáveis CSS globais.

```css
:root {
  --green-900: #314C36;
  --green-700: #4F6F52;
  --green-600: #5E7D5A;
  --green-100: #DDE8DA;
  --brown-800: #6E4F3A;
  --brown-600: #9B7256;
  --brown-200: #D8C0AE;
  --cream-100: #F7F3EE;
  --beige-100: #EFE6DC;
  --white: #FFFFFF;
  --text-primary: #2E2E2E;
  --text-secondary: #6A6A6A;
  --border: #D9D9D9;
  --success: #4F8A58;
  --warning: #C7943E;
  --danger: #B75A5A;
  --info: #5B7FA3;
}
```

## 4.2 Tipografia

Preferência: Inter. Se não houver carregamento externo, usar fallback seguro.

```css
font-family: Inter, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
```

## 4.3 Componentes

- cartões com 14–18px de radius;
- botões com altura mínima de 44px;
- contraste adequado;
- estados hover/focus/disabled/loading;
- formulários claramente rotulados;
- sidebar nos dashboards desktop;
- navegação adaptada a mobile;
- skeletons/loading para telas importantes;
- toasts para sucesso/erro;
- modais com foco acessível;
- breadcrumbs quando houver profundidade de navegação.

---

# 5. PERFIS DO SISTEMA

O protótipo terá **quatro perfis**:

1. **Paciente**
2. **Médico**
3. **Suporte**
4. **Administrador**

O Admin é separado do Suporte.

---

# 6. CONTROLE DE ACESSO E PERMISSÕES — RBAC

Implementar RBAC demonstrativo.

## 6.1 Admin

O Admin possui todas as permissões administrativas do protótipo.

## 6.2 Suporte

O Suporte possui permissões granulares. Um Admin consegue marcar/desmarcar permissões por usuário de suporte.

Permissões sugeridas:

```text
support.tickets.view
support.tickets.reply
support.tickets.transfer
support.tickets.close
support.tickets.reopen
support.transcript.send
support.attachments.upload
users.view
users.create
users.edit
users.activate
users.deactivate
users.reset_password
appointments.view_operational
payments.view_operational
consents.view_status
logs.view_limited
```

Não conceder automaticamente acesso a prontuário clínico, pré-anamnese detalhada ou documentos clínicos.

## 6.3 Princípio do menor privilégio

- Suporte vê apenas informações necessárias para resolver problemas técnicos, cadastrais, financeiros ou operacionais.
- Admin também não deve ter uma tela genérica para ler conteúdo clínico por curiosidade.
- Conteúdo clínico deve ser acessado apenas em fluxos estritamente autorizados e auditados.
- No protótipo, representar visualmente campos clínicos como “Conteúdo restrito” para perfis sem permissão.

---

# 7. MATRIZ DE CAPACIDADES

## 7.1 Paciente

Pode:

- cadastrar conta;
- aceitar termos;
- login/logout;
- recuperar senha;
- editar perfil;
- visualizar consentimentos;
- consultar preço atualizado;
- pagar consulta simulada;
- preencher pré-anamnese;
- entrar em fila;
- testar câmera/microfone;
- entrar em teleconsulta;
- usar chat clínico com médico;
- enviar anexos permitidos;
- receber anexos do médico;
- consultar documentos;
- consultar histórico;
- pesquisar e filtrar histórico;
- abrir suporte;
- conversar com chatbot;
- transformar atendimento em ticket;
- conversar com suporte humano;
- enviar prints, documentos e áudio;
- receber transcrição por e-mail;
- acompanhar tickets;
- avaliar consulta;
- avaliar atendimento de suporte.

## 7.2 Médico

Pode:

- login/logout;
- recuperar senha;
- gerenciar perfil;
- alterar disponibilidade;
- ver fila;
- ver agenda;
- acessar pré-anamnese do paciente vinculado à consulta;
- iniciar teleconsulta;
- usar chat clínico;
- receber anexos do paciente;
- enviar orientações, documentos e anexos;
- registrar resumo/conduta demonstrativos;
- criar documentos médicos simulados;
- finalizar consulta;
- acessar histórico de seus atendimentos;
- pesquisar por paciente, data, consulta e outros filtros;
- visualizar financeiro demonstrativo;
- abrir tickets de suporte;
- conversar com chatbot e suporte humano.

## 7.3 Suporte

Pode conforme permissões:

- operar tickets;
- visualizar tickets aguardando;
- assumir ticket;
- transferir ticket;
- responder por texto;
- enviar áudio;
- enviar arquivos permitidos;
- visualizar anexos enviados pelo solicitante quando pertinentes ao ticket;
- criar notas internas não visíveis ao usuário;
- usar respostas rápidas/macros;
- alterar prioridade;
- classificar categoria;
- adicionar tags;
- encerrar/reabrir ticket;
- enviar transcrição;
- visualizar SLA demonstrativo;
- consultar histórico de interação;
- criar e gerenciar usuários se possuir as permissões correspondentes;
- visualizar status de consentimento sem ler necessariamente o conteúdo clínico;
- acessar dados operacionais de consultas;
- acessar dados operacionais de pagamentos.

## 7.4 Admin

Pode:

- criar usuário Paciente;
- criar usuário Médico;
- criar usuário Suporte;
- criar usuário Admin;
- editar todos os usuários;
- ativar/desativar/bloquear/desbloquear;
- redefinir senha de forma administrativa demonstrativa;
- definir permissões de Suporte;
- visualizar sessões demonstrativas;
- forçar logout;
- configurar valor da consulta;
- configurar valor previsto para repasse médico;
- publicar/despublicar campanhas no site;
- editar banners, títulos, subtítulos, CTAs e datas de campanha;
- configurar avisos do sistema;
- visualizar métricas;
- visualizar tickets e operação de suporte;
- gerenciar categorias/tags de tickets;
- visualizar registros de consentimento;
- acessar auditoria administrativa;
- gerenciar configurações gerais;
- manter histórico de todas as alterações administrativas.

---

# 8. ESTRUTURA DE PASTAS

```text
fernanda-azevedo/
├── pom.xml
├── README.md
├── AGENTS.md
├── src/
│   └── main/
│       ├── java/com/fernandaazevedo/
│       │   ├── Application.java
│       │   ├── auth/
│       │   ├── users/
│       │   ├── roles/
│       │   ├── permissions/
│       │   ├── appointments/
│       │   ├── anamnesis/
│       │   ├── consultation/
│       │   ├── clinicalchat/
│       │   ├── support/
│       │   ├── tickets/
│       │   ├── attachments/
│       │   ├── securityscan/
│       │   ├── consent/
│       │   ├── payments/
│       │   ├── campaigns/
│       │   ├── settings/
│       │   ├── notifications/
│       │   ├── audit/
│       │   └── common/
│       └── resources/
│           ├── application.properties
│           ├── static/
│           │   ├── css/
│           │   ├── js/
│           │   ├── assets/
│           │   └── uploads-demo/
│           └── templates/
│               ├── public/
│               ├── auth/
│               ├── paciente/
│               ├── medico/
│               ├── suporte/
│               └── admin/
└── docs/
```

Pode usar páginas HTML estáticas servidas pelo Spring Boot, sem necessidade de engine complexa.

---

# 9. SITE PÚBLICO

## 9.1 Home

Criar página inicial completa.

Header:

- logo Fernanda Azevedo;
- Início;
- Como funciona;
- Para pacientes;
- Para médicos;
- Dúvidas;
- Entrar;
- botão “Consultar agora”.

Hero:

**Saúde perto de você.**

Texto acolhedor explicando consulta online com clínico geral.

CTA principal: **Consultar agora**.

CTA secundário: **Como funciona**.

Exibir preço da consulta vindo das configurações do backend. Não escrever valor fixo diretamente no HTML.

Se o Admin alterar o valor, a home deve refletir a alteração após atualização/consulta à API.

## 9.2 Como funciona

1. Crie sua conta.
2. Faça o pagamento.
3. Responda algumas perguntas rápidas.
4. Entre na consulta com o médico.
5. Consulte seu histórico e documentos quando precisar.

## 9.3 Campanhas e publicidade interna

Criar áreas administráveis:

- banner principal secundário;
- cards promocionais;
- faixa de aviso;
- CTA promocional.

Campanha contém:

```text
id
name
title
subtitle
ctaLabel
ctaUrl
imageUrlMock
position
startAt
endAt
status
createdBy
updatedBy
createdAt
updatedAt
```

Admin pode publicar/despublicar. Site mostra apenas campanhas ativas e dentro do período.

---

# 10. AUTENTICAÇÃO

## 10.1 Login

Campos:

- e-mail;
- senha;
- lembrar-me;
- botão Entrar;
- Entrar com Google (simulado);
- link Esqueci minha senha;
- link Criar conta.

Não pedir tipo de usuário no login. O sistema identifica o perfil e redireciona.

Redirecionamento:

```text
PATIENT -> /paciente/dashboard
DOCTOR -> /medico/dashboard
SUPPORT -> /suporte/dashboard
ADMIN -> /admin/dashboard
```

Estados:

- carregando;
- credenciais inválidas;
- usuário desativado;
- usuário bloqueado;
- senha temporária;
- sessão expirada.

## 10.2 Recuperação de senha

Fluxo:

1. usuário informa e-mail;
2. sistema mostra mensagem neutra para evitar enumeração de contas;
3. simular envio de link/token;
4. página redefinir-senha;
5. nova senha + confirmação;
6. indicador de requisitos;
7. confirmação de alteração;
8. voltar ao login.

## 10.3 Senha

No protótipo, exigir no mínimo:

- 8 caracteres;
- letra maiúscula;
- letra minúscula;
- número;
- caractere especial.

Nunca armazenar senha em texto puro. No backend demonstrativo usar hash apropriado (BCrypt).

---

# 11. CADASTRO DE PACIENTE

Campos principais:

- nome completo;
- CPF mock validável em formato;
- data de nascimento;
- e-mail;
- telefone;
- senha;
- confirmação;
- checkbox de leitura/aceite dos Termos de Uso;
- checkbox destacado para Política de Privacidade quando aplicável ao fluxo;
- consentimentos separados quando necessário.

Não usar um único checkbox genérico para autorizar qualquer tratamento de dado de saúde.

Registrar evidência de aceite:

```text
consentId
userId
documentType
documentVersion
accepted
acceptedAt
ipMock
userAgent
source
revokedAt
```

O protótipo deve permitir abrir a versão do documento aceita.

---

# 12. LGPD E CONSENTIMENTOS NO PROTÓTIPO

Dados de saúde são tratados como dados pessoais sensíveis. O protótipo deve representar privacy by design.

## 12.1 Central de Privacidade do usuário

Paciente e Médico devem ter uma página “Privacidade e consentimentos” contendo:

- Termos de Uso aceitos;
- Política de Privacidade;
- versão;
- data/hora do aceite;
- status;
- finalidade resumida;
- link “visualizar documento”.

## 12.2 Admin e Suporte

Admin pode consultar registros de consentimento e versões.

Suporte pode consultar **status e evidência operacional de aceite** apenas se possuir `consents.view_status`.

Não criar botão indiscriminado de “revogar tudo” quando o tratamento puder depender de outras bases legais ou obrigações de guarda. No protótipo, a tela deve separar:

- preferências/consentimentos revogáveis;
- dados necessários à execução do serviço;
- informações sujeitas a obrigação legal/regulatória.

Exibir mensagem de que solicitações relativas a dados pessoais serão avaliadas conforme legislação e obrigações aplicáveis.

## 12.3 Auditoria

Toda visualização administrativa de consentimentos gera audit log.

---

# 13. FLUXO PRINCIPAL DO PACIENTE

Fluxo obrigatório:

```text
HOME
 -> CADASTRO/LOGIN
 -> CONSULTAR AGORA
 -> RESUMO E PAGAMENTO
 -> PAGAMENTO APROVADO
 -> PRÉ-ANAMNESE DIGITAL
 -> TESTE DE CÂMERA/MICROFONE
 -> FILA/SALA DE ESPERA
 -> TELECONSULTA
 -> FINALIZAÇÃO
 -> DOCUMENTOS/HISTÓRICO
 -> AVALIAÇÃO
```

A localização de médico pode ser simulada em paralelo à pré-anamnese.

---

# 14. PAGAMENTO DEMONSTRATIVO

Preço não pode ser hardcoded no front-end.

API de configurações retorna:

```json
{
  "consultationPrice": 69.90,
  "doctorBasePayout": 50.00,
  "currency": "BRL"
}
```

Admin consegue alterar preço.

Tela de pagamento:

- resumo da compra;
- preço atual;
- cartão simulado;
- PIX simulado;
- cupom futuro desabilitado/placeholder;
- checkbox de ciência de termos comerciais;
- estados: processando, aprovado, recusado, expirado.

Não armazenar dados reais de cartão no protótipo.

---

# 15. PRÉ-ANAMNESE DIGITAL

Após pagamento aprovado, iniciar pré-anamnese simples.

Não chamar a experiência de diagnóstico automático.

Texto:

**Antes de falar com o médico, conte um pouco sobre o que está acontecendo. Suas respostas serão enviadas ao profissional responsável pelo seu atendimento.**

Perguntas mínimas:

1. **Qual é a sua principal queixa?**
2. **Há quanto tempo começou?**
3. **Possui alguma alergia?**
4. **Possui alguma condição de saúde ou comorbidade conhecida?**
5. **Usa algum medicamento atualmente?**
6. **Existe outra informação importante que gostaria de contar ao médico?**

Permitir “Não sei informar” quando apropriado.

Mostrar barra de progresso.

Salvar rascunho.

Após conclusão:

- gerar resumo estruturado;
- associar à consulta;
- médico visualiza antes de atender;
- paciente pode visualizar o que informou;
- alterações posteriores devem ser versionadas no protótipo.

O assistente não deve diagnosticar, prescrever ou sugerir medicamento.

---

# 16. TESTE DE CÂMERA E MICROFONE

Antes da sala de espera:

- solicitar permissão de câmera;
- solicitar microfone;
- preview local;
- seletor de câmera quando disponível;
- seletor de microfone quando disponível;
- medidor simples de áudio;
- botão “Testar alto-falante” simulado;
- estados de permissão negada;
- instruções para liberar permissão;
- botão continuar apenas com confirmação do usuário.

Não gravar vídeo.

---

# 17. TELECONSULTA — SALA DE VÍDEO

## 17.1 Layout

Área principal:

- vídeo grande do médico;
- preview pequeno do paciente;
- identificação do médico;
- cronômetro da consulta;
- status de conexão;
- indicadores de câmera/microfone.

Barra de controles:

- microfone on/off;
- câmera on/off;
- configurações de dispositivo;
- abrir/fechar chat;
- abrir arquivos;
- finalizar/sair conforme perfil.

Para protótipo, usar preview local + vídeo/placeholder do outro participante, mas estruturar um `VideoSessionService` para futura troca por WebRTC/provider.

## 17.2 Não gravar por padrão

O protótipo não deve simular gravação automática da consulta como requisito padrão.

---

# 18. CHAT CLÍNICO PACIENTE ↔ MÉDICO

Este chat é diferente do suporte.

## 18.1 Participantes

Somente:

- paciente vinculado à consulta;
- médico responsável pela consulta.

## 18.2 Mensagens

Aceitar:

- texto;
- imagem segura;
- PDF;
- documentos permitidos;
- áudio opcional de curta duração no protótipo;
- arquivos clínicos permitidos.

Paciente pode enviar:

- exames;
- resultados;
- documentos;
- fotografias pertinentes ao atendimento;
- requisições anteriores;
- outros anexos permitidos.

Médico pode enviar:

- orientações;
- arquivos;
- atestado demonstrativo;
- solicitação de exame demonstrativa;
- encaminhamento demonstrativo;
- relatório/resumo;
- receita demonstrativa quando houver módulo correspondente;
- outros documentos associados à consulta.

## 18.3 Persistência

Mensagens ficam vinculadas a `consultationId`.

Após finalizar consulta:

- chat fica disponível no histórico;
- preferencialmente em modo somente leitura;
- novos contatos clínicos devem pertencer a novo atendimento ou fluxo específico futuro.

## 18.4 Indicadores

- enviado;
- entregue;
- lido;
- horário;
- remetente;
- anexo analisado/permitido/bloqueado.

---

# 19. SEGURANÇA DE ARQUIVOS E LINKS

A UI e a arquitetura devem prever validação em camadas.

## 19.1 Tipos permitidos no protótipo

Whitelist sugerida:

```text
.pdf
.png
.jpg
.jpeg
.webp
.txt
.doc
.docx
.wav
.mp3
.m4a
```

Limites demonstrativos:

- documentos/imagens: até 15 MB;
- áudio: até 20 MB;
- máximo de 10 anexos por mensagem/lote.

## 19.2 Tipos bloqueados explicitamente

Bloquear extensões executáveis e potencialmente perigosas, inclusive:

```text
.exe .msi .bat .cmd .com .scr .ps1 .vbs .js .jar .apk .dll .sh .iso
```

A extensão `.js` deve ser bloqueada como anexo do usuário apesar de JavaScript ser usado internamente pelo sistema.

## 19.3 Não confiar apenas na extensão

Criar serviço demonstrativo `FileSecurityScanService` com pipeline:

```text
UPLOAD RECEBIDO
 -> validar tamanho
 -> validar extensão
 -> validar MIME declarado
 -> validar assinatura/magic bytes quando possível
 -> renomear arquivo internamente com UUID
 -> impedir execução
 -> sanitizar nome original para exibição
 -> escanear malware (MOCK no protótipo)
 -> analisar risco de conteúdo/link (MOCK no protótipo)
 -> status CLEAN | QUARANTINED | BLOCKED | SCAN_FAILED
 -> somente CLEAN fica disponível ao destinatário
```

Nunca renderizar HTML enviado pelo usuário como HTML ativo.

Nunca abrir arquivo perigoso inline.

## 19.4 Links em chats

Links digitados devem:

- ser detectados;
- não executar automaticamente;
- mostrar domínio;
- abrir em nova aba com proteções apropriadas;
- passar por `LinkSafetyService` mock;
- sinalizar domínio suspeito no protótipo;
- permitir bloqueio visual de link classificado como perigoso.

Nunca gerar preview ativo de conteúdo não confiável.

## 19.5 Quarentena

Se um arquivo for suspeito:

- não entregar ao médico/paciente/suporte;
- exibir “Arquivo em análise” ou “Arquivo bloqueado por segurança”;
- registrar evento;
- permitir ao Admin visualizar metadados do evento sem executar o arquivo.

## 19.6 Segurança real futura

Deixar interfaces preparadas para integração futura com:

- antivírus/antimalware;
- sandbox;
- scanner de objetos armazenados;
- verificação de URLs;
- Content Disarm & Reconstruction quando necessário;
- armazenamento privado com URLs temporárias.

---

# 20. HISTÓRICO DE CONSULTAS

Histórico deve existir para Paciente e Médico.

## 20.1 Paciente

Filtros:

- intervalo de datas;
- mês/ano;
- número/ID da consulta;
- nome do médico;
- status;
- palavra-chave demonstrativa quando permitido.

Card/lista mostra:

- data;
- médico;
- status;
- duração;
- ID da consulta;
- botão “Ver detalhes”.

## 20.2 Médico

Filtros:

- data;
- ID da consulta;
- nome do paciente;
- status;
- motivo/queixa principal;
- período.

## 20.3 Detalhe

Exibir conforme permissão:

- dados básicos;
- pré-anamnese;
- horário de início/fim;
- chat clínico;
- anexos;
- documentos gerados;
- resumo do atendimento;
- avaliação do paciente;
- trilha de eventos relevantes.

Não mostrar dados de outro paciente/médico sem vínculo.

---

# 21. CENTRAL DE SUPORTE — VISÃO DO PACIENTE E MÉDICO

Paciente e Médico terão item **Suporte** no menu.

A Central possui:

- pesquisar artigos de ajuda;
- categorias de ajuda;
- chatbot inicial;
- “Meus tickets”;
- botão “Falar com o suporte”.

---

# 22. CHATBOT DE SUPORTE

## 22.1 Objetivo

Resolver dúvidas simples antes de gerar ticket humano.

Não é chatbot clínico e não deve dar diagnóstico médico.

## 22.2 Fluxo inicial

Perguntar algo como:

**Olá! Sou o assistente virtual da Fernanda Azevedo. Como posso ajudar?**

Opções:

- Não consigo entrar;
- Problema com pagamento;
- Problema com câmera ou microfone;
- Minha consulta não iniciou;
- Preciso localizar um documento;
- Quero atualizar meus dados;
- Outro assunto.

O bot responde a uma pequena árvore de perguntas frequentes.

Sempre oferecer:

- “Isso resolveu meu problema”;
- “Ainda preciso de ajuda”.

## 22.3 Conversão em ticket

Se não resolveu:

1. coletar categoria;
2. coletar resumo;
3. perguntar urgência operacional;
4. permitir anexo;
5. confirmar e-mail/contato;
6. criar Ticket;
7. incluir automaticamente toda a conversa anterior com o bot no histórico;
8. colocar na fila do Suporte;
9. informar número do ticket.

Exemplo: `FA-2026-001284`.

---

# 23. SISTEMA DE TICKETS

## 23.1 Modelo

```text
id
ticketNumber
requesterId
requesterRole
subject
category
subcategory
priority
status
assignedSupportId
source
createdAt
firstResponseAt
updatedAt
resolvedAt
closedAt
lastMessageAt
tags
```

Status:

```text
BOT_IN_PROGRESS
OPEN
WAITING_SUPPORT
IN_PROGRESS
WAITING_USER
RESOLVED
CLOSED
REOPENED
```

Prioridades:

- baixa;
- normal;
- alta;
- crítica operacional.

“Crítica” não significa triagem médica. Não usar o suporte técnico como emergência clínica.

## 23.2 Categorias iniciais

- Conta e acesso;
- Senha;
- Cadastro;
- Pagamento;
- Reembolso/financeiro;
- Consulta;
- Câmera e microfone;
- Arquivos/documentos;
- Erro do sistema;
- Privacidade/dados;
- Outro.

---

# 24. CHAT HUMANO DE SUPORTE

Após ticket ser assumido, abrir chat em tempo real simulado.

## 24.1 Recursos

Ambas as partes podem enviar:

- texto;
- prints/imagens;
- PDF/documentos permitidos;
- áudio gravado;
- anexos seguros.

Aplicar o mesmo pipeline de segurança de arquivos.

## 24.2 Áudio

Permitir:

- botão segurar/gravar ou iniciar/parar;
- contador;
- preview;
- cancelar;
- enviar;
- player no chat.

Solicitar permissão do microfone.

## 24.3 Suporte pode

- responder;
- anexar;
- gravar áudio;
- adicionar nota interna;
- transferir ticket;
- alterar categoria;
- alterar prioridade;
- inserir tag;
- usar resposta pronta;
- marcar aguardando usuário;
- resolver;
- fechar;
- reabrir conforme regra.

## 24.4 Notas internas

Notas internas:

- visíveis somente para Suporte/Admin;
- visualmente diferentes;
- nunca entram na transcrição enviada ao usuário.

---

# 25. HISTÓRICO DE SUPORTE

Nunca apagar silenciosamente mensagens/tickets do protótipo.

Registrar:

- criação;
- bot;
- mensagens;
- anexos;
- atribuições;
- transferências;
- mudança de status;
- mudança de prioridade;
- notas internas;
- encerramento;
- reabertura;
- envio de transcript.

Paciente/Médico vê seus próprios tickets.

Filtros:

- número;
- data;
- status;
- categoria;
- assunto.

Suporte/Admin podem filtrar por:

- solicitante;
- atendente;
- período;
- status;
- categoria;
- prioridade;
- tags.

---

# 26. TRANSCRIÇÃO DO TICKET POR E-MAIL

Ao resolver/fechar ticket, oferecer:

**Enviar uma cópia desta conversa para meu e-mail.**

Também disponibilizar ação manual no ticket.

Criar `TranscriptService`.

No protótipo, o envio pode ser mockado, mas deve gerar preview completo.

Transcript do usuário contém:

- número do ticket;
- assunto;
- data de abertura;
- data de encerramento;
- participantes por nome/função;
- mensagens do chatbot relevantes;
- mensagens entre usuário e suporte;
- nomes/metadados dos anexos permitidos;
- status final.

Não incluir:

- notas internas;
- campos administrativos restritos;
- informações de outros usuários.

Registrar `TRANSCRIPT_SENT` em auditoria/eventos.

---

# 27. DASHBOARD DO SUPORTE

Cards:

- tickets aguardando;
- em atendimento;
- aguardando usuário;
- resolvidos hoje;
- tempo médio simulado de primeira resposta;
- meus tickets;
- tickets não atribuídos.

Fila com:

- ticket;
- solicitante;
- tipo de usuário;
- assunto;
- categoria;
- prioridade;
- tempo aguardando;
- status;
- atendente.

Ações rápidas:

- assumir;
- abrir;
- transferir.

Se possuir permissões de usuário, menu adicional “Usuários”.

---

# 28. DASHBOARD ADMIN

Cards:

- pacientes ativos;
- médicos ativos;
- suportes ativos;
- consultas hoje;
- consultas concluídas;
- receita bruta simulada;
- repasses médicos simulados;
- tickets abertos;
- preço atual da consulta;
- campanhas ativas.

Gráficos simples em CSS/JS ou Canvas:

- consultas por período;
- receita simulada;
- tickets por categoria;
- novos usuários.

---

# 29. GESTÃO DE USUÁRIOS — ADMIN

Tabela:

- nome;
- e-mail;
- perfil;
- status;
- criado em;
- último acesso;
- ações.

Filtros:

- nome/e-mail;
- perfil;
- status;
- data de cadastro.

Ações:

- visualizar;
- editar;
- ativar;
- desativar;
- bloquear;
- desbloquear;
- forçar redefinição de senha;
- forçar logout;
- consultar status de termos/consentimentos;
- permissões, quando Suporte.

Toda alteração deve gerar log.

---

# 30. CRIAÇÃO DE USUÁRIO

Admin cria qualquer perfil.

Suporte cria apenas perfis autorizados pela permissão `users.create` e pelas regras configuradas.

Formulário muda por perfil.

## 30.1 Paciente

- nome;
- CPF;
- nascimento;
- e-mail;
- telefone;
- senha temporária.

## 30.2 Médico

- nome;
- CPF;
- e-mail;
- telefone;
- CRM;
- UF do CRM;
- especialidade;
- RQE quando aplicável ao protótipo;
- status de validação demonstrativo;
- senha temporária.

## 30.3 Suporte

- nome;
- e-mail;
- telefone;
- cargo interno;
- permissões granulares;
- senha temporária.

## 30.4 Admin

- nome;
- e-mail;
- telefone;
- senha temporária.

Exigir troca da senha temporária no primeiro login.

---

# 31. CONFIGURAÇÕES COMERCIAIS — ADMIN

Tela “Valores e operação”.

Campos:

- preço atual da consulta;
- repasse-base do médico;
- moeda;
- texto de preço exibido no site;
- ativar/desativar “Consultar agora”.

Ao salvar:

1. validar;
2. pedir confirmação;
3. salvar nova versão;
4. refletir no site e checkout;
5. registrar valor antigo e novo no audit log.

Nunca alterar retroativamente o valor de consultas já compradas. Cada consulta guarda `priceAtPurchase` e `doctorPayoutAtPurchase`.

---

# 32. GESTÃO DE CAMPANHAS — ADMIN

Admin pode:

- criar;
- editar;
- visualizar preview;
- publicar;
- pausar;
- programar início/fim;
- excluir apenas rascunhos sem histórico relevante;
- arquivar campanhas antigas.

Campos:

- título;
- subtítulo;
- CTA;
- link;
- imagem mock;
- posição;
- data inicial/final;
- público: todos/paciente/médico;
- status.

Toda publicação/alteração gera audit log.

---

# 33. MÉDICO — DASHBOARD

Exibir:

- status online/offline;
- consultas hoje;
- próximos atendimentos;
- pacientes na fila;
- ganhos do dia;
- ganhos do mês;
- pendências;
- atalhos para agenda, histórico e suporte.

---

# 34. MÉDICO — FILA E PRÉ-ANAMNESE

Ao abrir paciente:

- nome;
- idade;
- ID da consulta;
- horário de entrada;
- pré-anamnese estruturada;
- anexos pré-consulta permitidos;
- botão “Iniciar atendimento”.

Não mostrar informações de pacientes fora da fila/agenda/vínculo do médico.

---

# 35. DOCUMENTOS DA CONSULTA

No protótipo, criar documentos demonstrativos vinculados à consulta.

Tipos:

- atestado;
- declaração;
- solicitação de exame;
- encaminhamento;
- relatório;
- receita demonstrativa.

O protótipo deve marcar claramente documentos como demonstração quando não houver assinatura/validade real.

Dados:

```text
documentId
consultationId
type
title
createdByDoctorId
createdAt
status
fileReferenceMock
```

Paciente vê em Histórico/Documentos.

---

# 36. REGISTRO E AUDITORIA

Criar `AuditLog` para ações sensíveis.

Campos:

```text
id
actorUserId
actorRole
action
resourceType
resourceId
summary
beforeJsonSafe
afterJsonSafe
ipMock
userAgent
createdAt
```

Registrar no mínimo:

- login bem-sucedido/falho relevante;
- logout;
- reset de senha;
- criação/edição/ativação de usuário;
- mudança de permissão;
- alteração de preço;
- campanha criada/publicada;
- ticket transferido/encerrado;
- transcript enviado;
- consentimento aceito/alterado;
- arquivo bloqueado/quarentenado;
- acesso administrativo a evidência de consentimento;
- ações administrativas relevantes.

Nunca colocar senha, token completo, dados de cartão ou conteúdo clínico integral no audit log.

---

# 37. MODELO DE DADOS MÍNIMO

Criar entidades demonstrativas:

```text
User
Role
Permission
UserPermission
PatientProfile
DoctorProfile
SupportProfile
Appointment
PreAnamnesis
Consultation
ConsultationMessage
Attachment
MedicalDocument
Payment
Ticket
TicketMessage
TicketInternalNote
TicketEvent
ConsentRecord
TermsDocument
Campaign
SystemSetting
Notification
AuditLog
```

Relacionamentos devem ser coerentes e impedir vazamento entre usuários.

---

# 38. ENDPOINTS REST SUGERIDOS

## Auth

```text
POST /api/auth/login
POST /api/auth/logout
POST /api/auth/forgot-password
POST /api/auth/reset-password
GET  /api/auth/me
```

## Users

```text
GET    /api/users
POST   /api/users
GET    /api/users/{id}
PUT    /api/users/{id}
PATCH  /api/users/{id}/status
PUT    /api/users/{id}/permissions
POST   /api/users/{id}/force-password-reset
POST   /api/users/{id}/force-logout
```

## Settings

```text
GET /api/public/settings
GET /api/admin/settings
PUT /api/admin/settings/commercial
```

## Campaigns

```text
GET    /api/public/campaigns
GET    /api/admin/campaigns
POST   /api/admin/campaigns
PUT    /api/admin/campaigns/{id}
POST   /api/admin/campaigns/{id}/publish
POST   /api/admin/campaigns/{id}/pause
```

## Appointments

```text
POST /api/appointments/checkout
POST /api/appointments/{id}/payment/mock-approve
GET  /api/appointments/my
GET  /api/appointments/{id}
```

## Pré-anamnese

```text
GET  /api/appointments/{id}/pre-anamnesis
PUT  /api/appointments/{id}/pre-anamnesis
POST /api/appointments/{id}/pre-anamnesis/complete
```

## Clinical chat

```text
GET  /api/consultations/{id}/messages
POST /api/consultations/{id}/messages
POST /api/consultations/{id}/attachments
GET  /api/consultations/{id}/attachments/{attachmentId}
```

## Tickets

```text
GET  /api/tickets/my
POST /api/tickets
GET  /api/tickets/{id}
GET  /api/tickets/{id}/messages
POST /api/tickets/{id}/messages
POST /api/tickets/{id}/attachments
POST /api/tickets/{id}/assign
POST /api/tickets/{id}/transfer
POST /api/tickets/{id}/status
POST /api/tickets/{id}/transcript
```

## Consentimentos

```text
GET  /api/privacy/documents/current
POST /api/privacy/consents
GET  /api/privacy/my-consents
GET  /api/admin/consents
```

---

# 39. DADOS DE DEMONSTRAÇÃO

Criar seed determinístico.

## 39.1 Paciente

```text
Nome: João Almeida
E-mail: paciente@fernandaazevedo.demo
Senha: Demo@123
```

## 39.2 Médico

```text
Nome: Dra. Fernanda Azevedo
E-mail: medico@fernandaazevedo.demo
Senha: Demo@123
CRM: 00000-DEMO
```

Não usar CRM fictício em contexto que pareça válido de produção; marcar DEMO.

## 39.3 Suporte

```text
Nome: Marina Costa
E-mail: suporte@fernandaazevedo.demo
Senha: Demo@123
```

Permissões amplas de demonstração, exceto acesso clínico.

## 39.4 Admin

```text
Nome: Administrador Demo
E-mail: admin@fernandaazevedo.demo
Senha: Demo@123
```

Mostrar as contas demo somente em ambiente de desenvolvimento/demonstração.

---

# 40. ESTADOS E EXPERIÊNCIA

Toda tela importante deve prever:

- carregando;
- vazio;
- sucesso;
- erro;
- sem permissão;
- sessão expirada;
- offline/reconectando quando aplicável;
- dados não encontrados;
- confirmação antes de ação destrutiva.

Não deixar botões sem ação.

---

# 41. RESPONSIVIDADE

Testar layouts em:

- 360px;
- 390px;
- 768px;
- 1024px;
- 1366px;
- 1440px+.

Teleconsulta em mobile:

- vídeo ocupa maior área;
- chat abre em drawer;
- controles fixos na parte inferior;
- preview local reposicionável visualmente se simples de implementar.

Dashboard mobile:

- sidebar vira drawer/menu;
- tabelas importantes usam cards ou scroll horizontal controlado.

---

# 42. ACESSIBILIDADE

Implementar:

- labels reais;
- navegação por teclado;
- foco visível;
- aria-label em botões somente com ícone;
- contraste;
- mensagens de erro associadas aos campos;
- não depender apenas de cor;
- tamanho de alvo clicável adequado.

---

# 43. SEGURANÇA BÁSICA DO BACKEND DEMO

Mesmo sendo protótipo:

- hash de senha;
- autorização server-side por papel/permissão;
- validação de input;
- nunca confiar em role vindo do browser;
- sanitizar nomes de arquivos;
- gerar IDs internos;
- limitar uploads;
- restringir Content-Type;
- não expor stack trace na UI;
- cookies de sessão com configurações seguras quando aplicável;
- headers básicos de segurança;
- proteção contra acesso direto a recursos de outro usuário.

Não implementar autorização apenas escondendo botões no front-end.

---

# 44. PRIVACIDADE E CONFORMIDADE — DIRETRIZ DO PROTÓTIPO

O protótipo deve demonstrar:

- minimização de dados;
- finalidade;
- segregação de acesso;
- consentimentos/versionamento quando usados;
- central de privacidade;
- trilha de auditoria;
- proteção de dados sensíveis;
- segurança desde a concepção;
- evidência de aceites;
- acesso restrito ao histórico clínico.

Este protótipo **não deve ser apresentado como juridicamente homologado para produção**. Antes do lançamento real, requisitos LGPD, CFM, prontuário/SRES, assinatura de documentos, guarda, segurança, bases legais, termos e operação devem ser validados por profissionais jurídicos, de privacidade e pela direção técnica médica.

---

# 45. REGRAS DE HISTÓRICO

## 45.1 Consulta

Nada relevante da consulta deve desaparecer ao atualizar a página.

Persistir no H2:

- consulta;
- pré-anamnese;
- mensagens;
- anexos/metadados;
- documentos;
- status;
- timestamps.

## 45.2 Suporte

Persistir:

- bot;
- ticket;
- mensagens;
- anexos/metadados;
- áudio/metadados;
- eventos;
- atendente;
- transcrição.

## 45.3 Alterações administrativas

Preço, permissões e campanhas devem ter histórico/audit log.

---

# 46. NOTIFICAÇÕES

Criar central de notificações.

Exemplos:

Paciente:

- pagamento aprovado;
- médico disponível;
- consulta finalizada;
- novo documento;
- resposta do suporte;
- ticket encerrado.

Médico:

- paciente pronto;
- nova mensagem/anexo;
- alteração operacional;
- resposta do suporte.

Suporte:

- novo ticket;
- ticket transferido;
- resposta do usuário.

Admin:

- evento administrativo importante;
- arquivo bloqueado;
- mudanças de configuração.

---

# 47. PÁGINAS OBRIGATÓRIAS

## Público/Auth

```text
/
/login
/cadastro
/esqueci-senha
/redefinir-senha
/termos
/privacidade
```

## Paciente

```text
/paciente/dashboard
/paciente/consultar
/paciente/pagamento
/paciente/pre-anamnese
/paciente/teste-dispositivos
/paciente/fila
/paciente/consulta/{id}
/paciente/historico
/paciente/historico/{id}
/paciente/documentos
/paciente/suporte
/paciente/suporte/tickets
/paciente/suporte/tickets/{id}
/paciente/perfil
/paciente/privacidade
```

## Médico

```text
/medico/dashboard
/medico/fila
/medico/agenda
/medico/consulta/{id}
/medico/historico
/medico/historico/{id}
/medico/financeiro
/medico/suporte
/medico/suporte/tickets
/medico/suporte/tickets/{id}
/medico/perfil
/medico/privacidade
```

## Suporte

```text
/suporte/dashboard
/suporte/tickets
/suporte/tickets/{id}
/suporte/usuarios
/suporte/usuarios/novo
/suporte/usuarios/{id}
/suporte/perfil
```

Rotas de usuários devem respeitar permissões.

## Admin

```text
/admin/dashboard
/admin/usuarios
/admin/usuarios/novo
/admin/usuarios/{id}
/admin/permissoes
/admin/consultas
/admin/pagamentos
/admin/tickets
/admin/campanhas
/admin/campanhas/novo
/admin/configuracoes
/admin/configuracoes/valores
/admin/consentimentos
/admin/auditoria
/admin/perfil
```

---

# 48. ORDEM DE IMPLEMENTAÇÃO PARA O CODEX

Implementar em etapas, mas continuar até o protótipo estar completo.

## Etapa 1 — Base

- Spring Boot;
- estrutura de diretórios;
- CSS global;
- componentes comuns;
- header/sidebar;
- H2;
- seed.

## Etapa 2 — Auth

- login;
- logout;
- sessão;
- recuperação de senha;
- redefinição;
- guards por role/permissão.

## Etapa 3 — Site público

- home;
- preço dinâmico;
- campanhas;
- cadastro;
- termos/privacidade.

## Etapa 4 — Paciente

- dashboard;
- pagamento mock;
- pré-anamnese;
- dispositivo;
- fila;
- sala de consulta;
- histórico.

## Etapa 5 — Médico

- dashboard;
- agenda;
- fila;
- pré-anamnese;
- sala;
- documentos;
- histórico;
- financeiro.

## Etapa 6 — Chat clínico e arquivos

- mensagens;
- anexos;
- scanner mock;
- quarentena;
- histórico.

## Etapa 7 — Suporte

- chatbot;
- tickets;
- chat humano;
- áudio;
- anexos;
- notas internas;
- transcrição;
- histórico.

## Etapa 8 — Admin

- usuários;
- permissões;
- preços;
- campanhas;
- tickets;
- consentimentos;
- auditoria;
- configurações.

## Etapa 9 — Refinamento

- responsividade;
- acessibilidade;
- estados vazios;
- loaders;
- erros;
- toasts;
- validações;
- dados demo.

## Etapa 10 — QA ponta a ponta

Executar os cenários de aceite abaixo e corrigir tudo que impedir a demonstração.

---

# 49. CENÁRIOS DE ACEITE OBRIGATÓRIOS

## Cenário A — Paciente completo

1. abrir home;
2. ver preço atual;
3. criar conta/aceitar termos;
4. login;
5. consultar agora;
6. pagamento simulado;
7. pré-anamnese;
8. teste de câmera/mic;
9. entrar na fila;
10. entrar na teleconsulta;
11. conversar com médico;
12. enviar PDF/imagem permitida;
13. tentar enviar `.exe` e receber bloqueio;
14. receber documento do médico;
15. finalizar;
16. encontrar consulta no histórico por data/médico;
17. abrir detalhes e ver chat/anexos/documentos.

## Cenário B — Médico completo

1. login;
2. ficar disponível;
3. abrir paciente da fila;
4. ler pré-anamnese;
5. iniciar consulta;
6. conversar;
7. abrir arquivo seguro;
8. enviar documento;
9. finalizar;
10. encontrar paciente no histórico por nome;
11. consultar financeiro.

## Cenário C — Suporte completo

1. paciente abre suporte;
2. chatbot tenta resolver;
3. paciente escolhe “Ainda preciso de ajuda”;
4. ticket é criado com histórico do bot;
5. suporte recebe ticket;
6. assume;
7. conversa com paciente;
8. paciente envia print e áudio;
9. suporte envia arquivo seguro;
10. suporte adiciona nota interna;
11. transfere para outro suporte;
12. segundo suporte continua do mesmo histórico;
13. resolve;
14. gera transcript;
15. simula envio por e-mail;
16. paciente vê ticket encerrado no histórico.

## Cenário D — Admin altera preço

1. login Admin;
2. abrir configurações;
3. mudar preço;
4. salvar;
5. audit log registra antes/depois;
6. abrir home em nova sessão;
7. novo preço aparece;
8. checkout usa novo preço;
9. consulta antiga mantém preço antigo.

## Cenário E — Campanha

1. Admin cria banner;
2. salva rascunho;
3. preview;
4. publica;
5. home passa a exibir;
6. pausa;
7. home deixa de exibir;
8. histórico de alteração permanece.

## Cenário F — Permissão do Suporte

1. Admin remove `users.create` do suporte;
2. Suporte atualiza sessão;
3. botão de criação desaparece;
4. tentativa direta via endpoint retorna 403;
5. Admin reativa permissão;
6. função volta a ficar disponível.

## Cenário G — LGPD/consentimento

1. paciente aceita Termos versão X;
2. sistema registra data/hora/versão;
3. paciente vê em Privacidade;
4. Admin consulta evidência;
5. audit log registra acesso administrativo;
6. Suporte sem permissão não acessa;
7. Suporte com `consents.view_status` vê apenas status/evidência operacional autorizada.

---

# 50. CRITÉRIO DE “PROTÓTIPO COMPLETO”

O trabalho só deve ser considerado concluído quando:

- as quatro experiências de perfil estiverem navegáveis;
- login e recuperação de senha funcionarem;
- RBAC funcionar no backend;
- Paciente conseguir percorrer a jornada completa;
- Médico conseguir percorrer a jornada completa;
- suporte bot → ticket → humano → transcript funcionar;
- anexos seguros/bloqueados forem demonstráveis;
- áudio no suporte for demonstrável;
- chat clínico persistir no histórico;
- histórico for pesquisável;
- Admin puder gerir usuários;
- Admin puder editar valor de consulta em tempo real;
- Admin puder gerir campanhas;
- consentimentos tiverem evidência/versionamento;
- audit logs existirem;
- o sistema for responsivo;
- não existirem links principais ou botões importantes sem ação;
- dados demo estiverem pré-carregados;
- README explicar como iniciar o projeto.

---

# 51. README OBRIGATÓRIO

Criar `README.md` contendo:

- nome do projeto;
- objetivo;
- requisitos;
- versão Java;
- como executar;
- URL local;
- contas demo;
- estrutura;
- funcionalidades;
- limitações do protótipo;
- itens simulados;
- próximos passos para produção.

---

# 52. PROIBIÇÕES PARA O CODEX

Não:

- entregar apenas uma landing page;
- entregar dashboards sem navegação;
- usar dados clínicos reais;
- usar CPF/CRM reais nos seeds;
- armazenar senha em texto puro;
- aceitar executáveis como anexos;
- confiar somente em extensão de arquivo;
- permitir suporte acessar prontuário completo por padrão;
- apagar histórico importante para simplificar;
- hardcodar preço em várias páginas;
- implementar autorização apenas no JavaScript;
- criar diagnóstico automático na pré-anamnese;
- afirmar que o scanner mock detecta malware de verdade;
- afirmar que documento demo possui validade médica real;
- gravar teleconsulta por padrão;
- incluir notas internas no transcript enviado ao usuário.

---

# 53. RESULTADO ESPERADO

Ao iniciar a aplicação, deve ser possível demonstrar a plataforma **Fernanda Azevedo** como se fosse um produto em estágio avançado de desenvolvimento: site institucional, autenticação, jornada de compra, pré-anamnese, teleconsulta, chat clínico seguro, documentos, histórico, central de suporte com chatbot e tickets, atendimento humano multimídia, gestão de usuários, permissões, configurações comerciais, campanhas, consentimentos e auditoria.

A prioridade é alta fidelidade visual + coerência operacional + segurança por desenho + código organizado para evolução posterior.

---

# 45. ATUALIZAÇÃO OBRIGATÓRIA — FOTOGRAFIAS OFICIAIS, MARKETING E ÍCONE DO CHATBOT

> Esta seção é obrigatória e complementa/sobrescreve qualquer orientação anterior sobre imagens genéricas, hero, seção institucional ou avatar do chatbot. O protótipo deve utilizar as fotografias reais fornecidas para a identidade pública do sistema **Fernanda Azevedo**.

## 45.1 Fotografias oficiais fornecidas

O projeto deve conter as duas fotografias originais, sem reprocessamento destrutivo, com os seguintes nomes de arquivo dentro do projeto:

```text
src/main/resources/static/assets/marketing/fernanda-azevedo-consultorio-horizontal.jpg
src/main/resources/static/assets/marketing/fernanda-azevedo-retrato-vertical.jpg
```

Correspondência:

- `fernanda-azevedo-consultorio-horizontal.jpg`: fotografia horizontal da Dra. Fernanda Azevedo sentada no consultório utilizando o computador.
- `fernanda-azevedo-retrato-vertical.jpg`: fotografia vertical da Dra. Fernanda Azevedo sentada no consultório olhando para a câmera.

Os arquivos entregues junto com este AGENTS.md devem ser copiados para esses caminhos **preservando os bytes originais sempre que possível**. Não recomprimir apenas para reduzir tamanho nesta fase do protótipo.

## 45.2 Regra absoluta de fidelidade física

As fotografias devem ser usadas **sem modificar características físicas da pessoa retratada**.

É proibido no protótipo:

- alterar rosto, formato facial ou expressão;
- afinar ou modificar corpo;
- mudar tom de pele;
- alterar cabelo, olhos, nariz, boca ou dentes;
- aplicar embelezamento por IA;
- substituir roupas por geração de imagem;
- alterar proporções corporais;
- aplicar filtros que descaracterizem a fotografia;
- gerar uma “nova Fernanda” baseada nas fotos;
- usar face swap;
- remover ou adicionar elementos sobre o corpo com IA;
- fazer retoque que mude aparência pessoal.

São permitidos somente ajustes de apresentação não destrutivos no navegador, como:

- `object-fit: cover`;
- `object-position`;
- border-radius;
- máscara visual CSS;
- sombra leve;
- overlay de contraste sobre áreas sem rosto quando necessário para legibilidade;
- crop responsivo feito pelo container CSS, sem editar o arquivo-fonte;
- redução visual de tamanho via CSS mantendo proporção.

Não sobrescrever os JPG originais após qualquer processamento.

## 45.3 Qualidade e carregamento

As imagens devem manter boa qualidade visual em desktop e mobile.

Regras:

- usar o JPG original como fonte principal;
- não esticar imagem fora da proporção;
- definir `width` e `height`/`aspect-ratio` para evitar layout shift;
- usar `loading="lazy"` nas imagens abaixo da dobra;
- a imagem principal acima da dobra pode usar carregamento prioritário;
- nunca usar a imagem vertical deformada em container horizontal;
- não aplicar blur permanente ou filtros de baixa qualidade;
- se futuramente forem criadas versões WebP/AVIF, manter o original como fallback e não excluir o arquivo original;
- nesta fase, não é necessário converter/recomprimir as imagens.

## 45.4 Uso da foto horizontal — HOME / HERO

A fotografia horizontal deve ser utilizada prioritariamente no hero ou em uma seção de destaque inicial da Home.

Composição sugerida em desktop:

```text
---------------------------------------------------------------
| Header                                                       |
---------------------------------------------------------------
| Texto / proposta de valor       | Foto horizontal Fernanda   |
|                                 | no consultório             |
| Saúde perto de você.            |                            |
| Consulta online simples...      |                            |
| [Consultar agora] [Como funciona]|                           |
| Preço dinâmico                  |                            |
---------------------------------------------------------------
```

Regras visuais:

- imagem deve parecer parte natural da marca, não um banner publicitário genérico;
- manter o rosto totalmente visível no crop principal;
- usar `object-position` adequado para preservar a pessoa na composição;
- não colocar texto diretamente sobre o rosto;
- em telas pequenas, empilhar texto e fotografia;
- o CTA principal continua destacado em verde;
- a fotografia pode ficar dentro de card com cantos arredondados, mas evitar excesso de molduras.

## 45.5 Uso da foto vertical — seção institucional / valores

A fotografia vertical deve ser utilizada em uma seção pública de marca, por exemplo:

**“Cuidado pensado para estar mais perto de você.”**

Ao lado da fotografia, apresentar a filosofia do sistema sem linguagem elitista.

Texto-base sugerido para o protótipo:

> A Fernanda Azevedo nasceu com uma proposta simples: aproximar pacientes e médicos por meio de uma experiência digital acolhedora, clara e segura. Tecnologia deve facilitar o cuidado, não criar novas barreiras.

Complementar com três ou quatro valores em cards:

- **Proximidade** — comunicação simples e humana durante toda a jornada.
- **Acesso** — uma experiência fácil de entender e utilizar.
- **Segurança** — privacidade e proteção dos dados desde a concepção do sistema.
- **Cuidado** — tecnologia como apoio à relação entre paciente e médico.

Não afirmar especialidade médica, título acadêmico, experiência profissional, registro de conselho ou qualquer credencial específica que não esteja cadastrada como dado oficial no sistema.

## 45.6 Seção “Sobre a Fernanda Azevedo”

Criar seção institucional pública opcional, acessível pela Home, com a fotografia vertical.

Objetivo:

- humanizar a marca;
- explicar por que o produto existe;
- aproximar o paciente;
- apresentar propósito e valores;
- reforçar que o sistema foi pensado em torno da experiência de cuidado.

Não transformar a página em perfil de celebridade nem em site pessoal. **Fernanda Azevedo é o nome da plataforma**, e a presença da médica deve reforçar a origem humana e o propósito da marca.

Exemplo de hierarquia:

```text
Sobre a Fernanda Azevedo
Cuidado, tecnologia e proximidade.

[foto vertical]  [texto institucional]
                 [Proximidade] [Acesso]
                 [Segurança]   [Cuidado]
```

## 45.7 Marketing sem promessas médicas indevidas

Os banners, campanhas e textos públicos administrados pelo Admin não podem utilizar promessas como:

- “cura garantida”;
- “diagnóstico garantido”;
- “o melhor médico do Brasil”;
- “resultado garantido”;
- “substitui qualquer atendimento presencial”;
- alegações clínicas não comprovadas.

Para o protótipo, usar mensagens centradas em acesso e experiência, por exemplo:

- “Saúde perto de você.”
- “Converse com um médico de onde estiver.”
- “Seu atendimento, histórico e documentos em um só lugar.”
- “Uma jornada simples do pagamento à consulta.”
- “Tecnologia para aproximar cuidado e pessoas.”

## 45.8 Componente reutilizável de imagem institucional

Criar componente/estrutura reutilizável para imagens de marketing com:

- `alt` descritivo;
- tratamento responsivo;
- fallback visual;
- skeleton durante carregamento quando necessário;
- nenhuma transformação facial;
- class names semânticas.

Exemplo:

```html
<figure class="brand-photo brand-photo--hero">
  <img
    src="/assets/marketing/fernanda-azevedo-consultorio-horizontal.jpg"
    alt="Médica Fernanda Azevedo em consultório"
    width="2048"
    height="1365"
  />
</figure>
```

E para a vertical:

```html
<figure class="brand-photo brand-photo--portrait">
  <img
    src="/assets/marketing/fernanda-azevedo-retrato-vertical.jpg"
    alt="Médica Fernanda Azevedo em consultório"
    width="1365"
    height="2048"
    loading="lazy"
  />
</figure>
```

## 45.9 Ícone oficial do chatbot — robô médico

O botão flutuante do chatbot deve utilizar um **robô médico simpático**, desenhado como ícone vetorial/SVG próprio do projeto.

Direção visual:

- formato amigável e simples;
- cabeça de robô arredondada;
- pequeno elemento médico, como cruz/estetoscópio estilizado;
- cores da identidade visual, principalmente verde, creme e pequenos detalhes marrons;
- expressão acolhedora;
- evitar aparência infantil demais;
- evitar aparência de robô futurista ameaçador;
- não copiar mascote/logotipo existente;
- funcionar bem em 32px, 40px, 48px e 56px;
- SVG leve, acessível e sem dependência externa.

Nome sugerido do asset:

```text
src/main/resources/static/assets/icons/medical-chatbot.svg
```

Botão flutuante:

- canto inferior direito;
- 56px em desktop;
- 52px em mobile;
- tooltip/`aria-label`: **“Abrir assistente de suporte”**;
- badge de notificação quando houver mensagem nova;
- animação sutil apenas quando apropriado;
- respeitar `prefers-reduced-motion`.

Quando aberto, o cabeçalho do chat deve mostrar o mesmo robô e o texto:

**Assistente Fernanda Azevedo**

Subtexto:

**Suporte virtual**

O chatbot continua estritamente voltado a suporte e navegação. Ele não deve se apresentar como médico, diagnosticar, prescrever ou substituir avaliação profissional.

## 45.10 Uso das fotos pelo painel Admin

As duas imagens oficiais devem vir cadastradas como **assets protegidos de marca** no protótipo.

O Admin pode:

- escolher em qual bloco de marketing cada foto será exibida;
- ativar/desativar uma seção;
- alterar título, subtítulo e CTA associados;
- alterar posicionamento visual predefinido;
- visualizar preview desktop/mobile.

O Admin **não pode**, dentro do protótipo:

- editar características físicas da pessoa;
- aplicar filtros de beleza;
- recortar e sobrescrever o arquivo original;
- excluir definitivamente o asset oficial sem confirmação especial.

Criar metadados demonstrativos:

```text
BrandAsset
- id
- type: PHOTO | ICON
- key
- originalFilename
- publicPath
- width
- height
- protectedAsset
- active
- createdAt
- updatedAt
```

## 45.11 Responsividade específica das fotografias

Desktop:

- hero em duas colunas;
- imagem horizontal ocupa aproximadamente 44% a 52% da largura útil;
- seção institucional usa foto vertical em coluna de aproximadamente 35% a 42%.

Tablet:

- reduzir gradualmente a imagem;
- evitar cortar cabeça, rosto, mãos de forma visualmente estranha;
- cards de valores podem virar grade 2x2.

Mobile:

- empilhar conteúdo;
- hero: texto primeiro, imagem depois;
- imagem horizontal em largura total do container;
- retrato vertical com altura máxima controlada e `object-fit: cover`;
- preservar rosto e parte superior do corpo no enquadramento;
- não usar `background-image` para a foto institucional quando isso prejudicar acessibilidade.

## 45.12 Testes obrigatórios desta seção

O Codex deve validar manualmente no protótipo:

1. as duas fotos carregam sem erro;
2. nenhuma fotografia está deformada;
3. o rosto não é cortado nos breakpoints principais;
4. não existe filtro CSS que altere tonalidade/aparência da pessoa;
5. os arquivos originais permanecem intactos;
6. os `alt` estão presentes;
7. o hero funciona em 360px, 768px, 1280px e 1440px;
8. a seção de valores permanece legível;
9. o robô médico aparece no botão de chatbot;
10. o botão abre o suporte;
11. o chatbot não emite orientação clínica;
12. alterações de marketing feitas pelo Admin atualizam os blocos públicos sem quebrar as fotografias.

---

# 46. REGRA DE ENTREGA DOS ASSETS DE MARCA

Ao iniciar o projeto, criar os diretórios:

```text
src/main/resources/static/assets/marketing/
src/main/resources/static/assets/icons/
```

Copiar os arquivos fornecidos junto com este documento da seguinte forma:

```text
fernanda-azevedo-consultorio-horizontal.jpg
fernanda-azevedo-retrato-vertical.jpg
```

Não usar fotos de banco de imagens para substituir essas duas fotografias nos blocos principais definidos nesta especificação.

Caso as fotografias ainda não estejam fisicamente presentes na pasta do projeto, a aplicação pode utilizar placeholder temporário durante desenvolvimento, mas o Codex deve deixar o caminho final preparado e registrar claramente a pendência no README. Na entrega final do protótipo com os assets disponíveis, os placeholders devem ser removidos.

---

# 47. PRIORIDADE DESTA ATUALIZAÇÃO

Em caso de conflito com seções anteriores:

1. preservar as regras de segurança, LGPD e menor privilégio já definidas;
2. usar as fotografias oficiais conforme esta atualização;
3. manter o nome **Fernanda Azevedo**;
4. usar o robô médico como identidade visual do chatbot de suporte;
5. preservar aparência física real da pessoa retratada;
6. priorizar proximidade, acesso, segurança e cuidado na comunicação pública.

