# Fernanda Azevedo — plataforma de telemedicina

Aplicação Spring Boot com autenticação/RBAC, jornada do paciente, agenda médica, teleconsulta WebRTC, chat clínico, suporte com tickets, administração, consentimentos e auditoria.

## Executar

Requer Java 21 e Maven 3.9+.

```powershell
mvn spring-boot:run
```

Acesse `http://localhost:8080`. O H2 persiste em `./data/fernanda-azevedo.mv.db`.

Nesta máquina, o Java 21 e o Maven portáteis estão em `.runtime`. Após reiniciar o computador, também é possível iniciar com:

```powershell
.\iniciar.ps1
```

O servidor é vinculado exclusivamente a `127.0.0.1`, portanto não é publicado na rede local nem na internet.

## Publicar no Render

O repositório inclui `Dockerfile` e `render.yaml`. No Render, crie um **Blueprint**, conecte este repositório e confirme a criação do serviço web e do PostgreSQL. O Render define a porta e publica a aplicação com HTTPS automaticamente.

Antes do primeiro deploy, informe no formulário do Blueprint:

- `BOOTSTRAP_ADMIN_EMAIL`: e-mail do primeiro administrador;
- `BOOTSTRAP_ADMIN_PASSWORD`: senha temporária forte, com ao menos 12 caracteres, letra maiúscula, minúscula, número e símbolo;
- `WEBRTC_TURN_URL`, `WEBRTC_TURN_USERNAME` e `WEBRTC_TURN_CREDENTIAL`: credenciais do servidor TURN para chamadas entre redes diferentes.

O perfil `production` desativa as contas e os dados demonstrativos. Na primeira inicialização de um banco vazio, somente o administrador informado acima é criado, com troca obrigatória de senha. O PostgreSQL do Blueprint mantém usuários, consultas, chats e tickets entre reinicializações.

O Blueprint usa planos gratuitos por padrão para não gerar cobrança automática. Para operação real contínua, revise retenção, backups e disponibilidade do plano do PostgreSQL e contrate armazenamento privado persistente para os anexos.

## Contas de desenvolvimento

Todas usam `Demo@123` (armazenada apenas como hash BCrypt).

| Perfil | E-mail |
|---|---|
| Paciente | `paciente@fernandaazevedo.demo` |
| Médico | `medico@fernandaazevedo.demo` |
| Suporte | `suporte@fernandaazevedo.demo` |
| Admin | `admin@fernandaazevedo.demo` |

## Recursos

- Site responsivo com fotos oficiais preservadas e preço/campanhas via API.
- Cadastro com consentimento versionado, login por perfil e recuperação neutra.
- Checkout, pré-anamnese sem diagnóstico, MediaDevices, fila e sala WebRTC.
- Chat clínico em tempo real por WebSocket, persistido por consulta, com entrega e leitura.
- Anexos clínicos em armazenamento privado, download autorizado, magic bytes, hash SHA-256, quarentena e adaptador ClamAV.
- Agenda médica persistente, presença online, reserva transacional, fila, atribuição exclusiva e estados de atendimento.
- Histórico, documentos marcados como demo, chats e upload com bloqueio de executáveis.
- Chatbot operacional, tickets, notas internas e transcript mock.
- Usuários, permissões, valores, campanhas, consentimentos e auditoria.

## Estrutura

- `src/main/java/com/fernandaazevedo`: backend, domínio, serviços e APIs.
- `src/main/resources/static`: HTML5, CSS e JavaScript modular.
- `src/main/resources/static/assets`: fotos oficiais e SVG do chatbot.

## Dependências ainda não produtivas

Pagamento, Google OAuth, antimalware, verificação de URLs, cloud, SMTP, assinatura e push são simulados. A videochamada WebRTC é real, não é gravada e ainda depende de TURN e validação entre redes distintas para uso confiável. O scanner mock **não oferece proteção antimalware real** e documentos demo não têm validade médica. Produção exige revisão jurídica/LGPD/CFM, SRES, infraestrutura, CSRF/CSP, storage privado, scanner real, OAuth, observabilidade e testes de segurança.

## WebRTC e TURN

A teleconsulta usa WebRTC real e sinalização WebSocket autenticada no mesmo servidor. O paciente e o médico precisam estar vinculados à mesma consulta ativa. STUN público é usado para desenvolvimento.

Para conectividade confiável entre redes diferentes, configure um servidor TURN por variáveis de ambiente:

```text
WEBRTC_TURN_URL=turns:turn.exemplo.com:5349
WEBRTC_TURN_USERNAME=usuario-temporario
WEBRTC_TURN_CREDENTIAL=credencial-temporaria
```

Sem TURN, a chamada pode funcionar na mesma rede ou em NATs compatíveis, mas não deve ser considerada pronta para produção. Credenciais TURN permanentes não devem ser versionadas; em produção, use credenciais temporárias emitidas por backend seguro.

O teste `scripts/webrtc-e2e.cjs` abre paciente e médico em contextos isolados do Chromium e exige áudio e vídeo local/remoto nos dois participantes. Ele usa dispositivos sintéticos do navegador, portanto valida a aplicação e a negociação WebRTC, não o hardware físico do computador.

## Antimalware e armazenamento privado

Os anexos são gravados fora da pasta pública usando nomes UUID e nunca são executados ou exibidos inline. A extensão, MIME declarado, tamanho e assinatura inicial são validados antes do armazenamento. Configure um daemon ClamAV acessível pelo protocolo `INSTREAM`:

```text
PRIVATE_STORAGE_ROOT=/caminho/privado
CLAMAV_HOST=clamav
CLAMAV_PORT=3310
CLAMAV_TIMEOUT_MS=10000
```

Se o ClamAV não estiver configurado ou falhar, o arquivo recebe `QUARANTINED` e não pode ser compartilhado nem baixado. Somente `CLEAN` é disponibilizado. No Render gratuito, o sistema de arquivos é efêmero; produção precisa de object storage privado, URLs temporárias, scanner de objetos e política de retenção.
