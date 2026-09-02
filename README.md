# Fernanda Azevedo — protótipo de telemedicina

Protótipo de alta fidelidade com site institucional, autenticação/RBAC, jornada do paciente, área médica, teleconsulta demonstrativa, chat clínico, suporte com chatbot e tickets, administração, consentimentos e auditoria.

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

## Contas demo

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
- Checkout, pré-anamnese sem diagnóstico, MediaDevices, fila e sala demonstrativa.
- Histórico, documentos marcados como demo, chats e upload com bloqueio de executáveis.
- Chatbot operacional, tickets, notas internas e transcript mock.
- Usuários, permissões, valores, campanhas, consentimentos e auditoria.

## Estrutura

- `src/main/java/com/fernandaazevedo`: backend, domínio, serviços e APIs.
- `src/main/resources/static`: HTML5, CSS e JavaScript modular.
- `src/main/resources/static/assets`: fotos oficiais e SVG do chatbot.

## Limitações

Pagamento, Google OAuth, vídeo remoto, antimalware, verificação de URLs, cloud, SMTP, assinatura e push são simulados. O preview local não é gravado. O scanner mock **não oferece proteção antimalware real** e documentos demo não têm validade médica. Produção exige revisão jurídica/LGPD/CFM, SRES, infraestrutura, CSRF/CSP, storage privado, scanner real, OAuth, observabilidade e testes de segurança.
