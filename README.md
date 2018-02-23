# HttpNIOServer

FileVoyageur.java - статический класс который ходит в ФС за файлами, хранит кэш в мапе (ключ: url(путь до файла), значение: FileVoyageurResponse) --- FileVoyageurResponse - суб класс хранящий БайтБуффер, последнее изменение, статус и код. Можно указать правило кэширования в сеттере FileVoyageur.setIsCaching(true/false)
HttpNIOServer.java - класс сервера, в нем в бесконичном цикле селектор обрабатывает ключи.
HttpRequestResponseManager.java - обработчик запросов, формирует http response в соответствие с заданной логикой.
HttpSession.java - класс сессии, на каждый файл открывается новая сессия.
