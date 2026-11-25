# Enos Music (projeto Android)

Aplicação Android de leitor de música local com interface simples inspirada no ecrã de reprodução apresentado pelo utilizador. Inclui lista de músicas, seleção por cantor, controlos básicos e tentativa de iniciar reprodução quando um dispositivo Bluetooth se liga.

## O que está incluído
- Leitura de ficheiros de áudio indexados pelo Android MediaStore, após autorização do utilizador.
- Reprodução em segundo plano com Media3/ExoPlayer e MediaSession.
- Ecrã de reprodução com capa genérica, título/artista, progresso, aleatório, anterior, play/pause e seguinte.
- Lista de músicas e lista de artistas.
- Receiver de eventos Bluetooth A2DP/Headset e ACL que tenta iniciar reprodução automaticamente.

## Limitação importante do Bluetooth
O Android (especialmente Android 12+) limita o início de serviços de reprodução a partir de eventos em segundo plano. A aplicação tenta iniciar automaticamente quando recebe a ligação Bluetooth, mas o comportamento depende da versão Android, do fabricante e do sistema multimédia do veículo. Em alguns Samsung pode ser necessário permitir Enos Music em **Definições > Aplicações > Enos Music > Bateria > Sem restrições** e ativar notificações. A primeira utilização requer abrir a aplicação e conceder acesso às músicas.

## Construir APK
Abrir a pasta `EnosMusic` no Android Studio com Android SDK 35 e JDK 17. Sincronizar Gradle e executar `app`. Para criar APK: **Build > Build APK(s)**.

Este projeto é um ponto de partida de código-fonte; não é um APK já instalado ou testado num veículo. É necessário compilar e testar no teu Samsung e no Bluetooth do carro para validar a reprodução automática.
