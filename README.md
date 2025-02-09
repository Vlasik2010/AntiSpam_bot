# 🛡️ AntiSpamBot

![GitHub repo size](https://img.shields.io/github/repo-size/your-username/AntiSpamBot?color=blue&style=for-the-badge)
![GitHub stars](https://img.shields.io/github/stars/your-username/AntiSpamBot?style=for-the-badge)
![GitHub forks](https://img.shields.io/github/forks/your-username/AntiSpamBot?style=for-the-badge)
![GitHub last commit](https://img.shields.io/github/last-commit/your-username/AntiSpamBot?style=for-the-badge)

## 🚀 О проекте
**AntiSpamBot** — это мощный Telegram-бот для защиты чатов от спама. Он использует передовые алгоритмы анализа текста, такие как нормализация, фуззи-сравнение и анализ повторяющихся сообщений, чтобы выявлять и удалять подозрительные сообщения.

## 🛠️ Функционал
✅ Фильтрация сообщений по запрещённым словам
✅ Определение спама по частоте отправки сообщений
✅ Фуззи-сравнение для обнаружения маскированных слов
✅ Интерактивное управление через inline-кнопки
✅ Поддержка команды `/editbanned` для изменения списка запрещённых слов
✅ Поддержка команд `/status`, `/help`, `/start`

## 🛡 Как работает анти-спам алгоритм?

1️⃣ **Фильтрация сообщений**
   - Определение флуда (слишком частые сообщения)
   - Детектирование дублирующихся сообщений
   - Обнаружение запрещённых слов

2️⃣ **Фуззи-сравнение (алгоритм Левенштейна)**
   - Распознавание завуалированных слов (sp@m, s.p.a.m, sp4m)
   - Поиск схожих слов в сообщении

3️⃣ **Inline-редактирование списка спама**
   - Используйте `/editbanned`, чтобы добавить или удалить запрещённое слово
   - Простое управление через Telegram-интерфейс

## 📌 Доступные команды

| Команда      | Описание                               |
|-------------|--------------------------------------|
| `/start`    | Приветственное сообщение             |
| `/help`     | Список доступных команд              |
| `/status`   | Статистика работы бота               |
| `/editbanned` | Добавление/удаление запрещённых слов |

## 📦 Структура проекта
```
📦 AntiSpamBot
 ┣ 📂 src
 ┃ ┗ 📂 com/example/antispambot
 ┃ ┃ ┣ 📜 Main.java              # Точка входа
 ┃ ┃ ┣ 📜 AntiSpamBot.java       # Основная логика бота
 ┃ ┃ ┣ 📜 BotConfig.java         # Конфигурация
 ┃ ┃ ┣ 📜 SpamFilterService.java # Алгоритм анти-спама
 ┣ 📜 pom.xml
 ┣ 📜 README.md
 ┣ 📜 LICENSE
```

## 🔧 Установка и запуск
### 📌 Требования:
- **Java 8+**
- **Maven**

### 📥 Клонирование проекта:
```bash
  git clone https://github.com/your-username/AntiSpamBot.git
  cd AntiSpamBot
```

### 🔧 Сборка проекта:
```bash
  mvn clean package
```

### ▶ Запуск бота:
```bash
  java -jar target/AntiSpamBot-1.0.0-jar-with-dependencies.jar
```

## ⚙️ Конфигурация
Перед запуском создайте файл `config.properties` в `src/main/resources/` и добавьте ваши данные:
```properties
bot.token=ВАШ_ТОКЕН
bot.username=ВАШ_ЮЗЕРНЕЙМ
```

## 🧑‍💻 Разработчики
👤 **Ваше Имя**  
🔗 [GitHub](https://github.com/your-username)  
🔗 [Telegram](https://t.me/your-telegram)

## 📜 Лицензия
Этот проект лицензирован под MIT License - смотрите [LICENSE](LICENSE) для деталей.

---
⭐️ Поддержите проект, поставив звезду на GitHub!

