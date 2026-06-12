# Практика 6. CI\CD
## Завдання
Побудувати власний CI/CD pipeline з такими кроками:
- аналіз коду на відповідність стилю (статичний аналіз коду)
- автоматизоване тестування
- збирання проєкту в jar
- розгортання нової версії (якщо сервер) або додавання у репозиторій артефактів jar файлу чи docker image

## Реалізація
Пайплайн реалізований за допомогою GitHub Actions. Автоматизація запускається у разі здійснення `push` або `pull_request` у гілку `main` та містить етапи, описані нижче.

1. Ініціалізація віртуальної машини `ubuntu-latest` та встановлення Java (JDK 25 Temurin).
2. Перевірка якості та форматування коду за допомогою `maven-checkstyle-plugin`.
3. Запуск юніт-тестів (JUnit) командою `mvn test`.
4. Компіляція та створення виконуваного `.jar` файлу (`maven-jar-plugin`).
5. Завантаження `.jar` файлу в систему артефактів GitHub Actions (`actions/upload-artifact@v6.0.0`).
6. Збирання Docker-образу на базі `eclipse-temurin:25-jdk-alpine` та його автоматичне відправлення у GitHub Container Registry `ghcr.io`.
  
## Конфігурація
1. `docker pull ghcr.io/kastashenkova/java-practice6/java-app:latest` (завантаження останньої версії образу)
2. `docker run ghcr.io/kastashenkova/java-practice6/java-app:latest` або `docker run -p [порт_вашого_комп'ютера]:[порт_всередині_контейнера] ghcr.io/kastashenkova/java-practice6/java-app:latest` (запуск контейнера з прокиданням порту або без)
