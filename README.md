# Explore-with-me

> ExploreWithMe (EWM) -этот сервис позволяет пользователям делиться информацией об интересных событиях и находить компанию для участия в них. 
> Данное приложение можно представить в виде афиши. Только в этой афише пользователи могут не только рассказать про какое-либо событие (например, про фотовыставку или про премьеру в театре), но  и собрать компанию для участия в нём.

## Оглавление
- [Функциональность приложения](#функциональность-приложения)
- [Схема БД и модели данных](#схема-бд-и-модели-данных)
- [Описание API](#описание-api)
- [Сборка и установка](#сборка-и-установка)
- [Стек технологий](#стек-технологий)

## Функциональность приложения
EWM - имеет микросервисную архитектуру. Состоит из 2-х сервисов:
1. **main-service** - основной сервис содержит всё необходимое для функционирования приложения
2. **stats** - сервис статистики хранит количество просмотров каждого события

API основного сервиса разделен на три части:
- **публичная** (pub) доступна без регистрации любому пользователю сети:
  - просмотр события с подробной информацией
  - поиск и фильтрация событий по количеству просмотров, либо по датам событий
  - каждое событие должно относиться к какой-то из закреплённых в приложении категорий
- **закрытая** (priv) доступна только авторизованным пользователям:
  - добавлять новые события
- **административная** (admin) — для администраторов сервиса

## Схема БД и модели данных
Модели:
- **Item** (Вещь)
- **ItemRequest** (Запрос)
- **User** (Пользователь)
- **Booking** (Бронирование)
- **Comment** (Отзыв)

Схема БД:
![Scheme of ExploreWithMe database](/ewm_diagram.png)

## Описание API
<details>
  <summary><h3>Вещь</h3></summary>
  
- **POST** /items - добавление новой вещи
- **PATCH** /items/{itemId} - обновление информации о вещи
- **GET** /items/{itemId} - просмотр информации о конкретной вещи по её идентификатору
- **GET** /items - просмотр владельцем списка всех его вещей с указанием названия и описания для каждой
- **GET** /items/search - поиск вещи по ключевым словам в описании или в названии вещи
- **DELETE** /items/{itemId} - удаление вещи из сервиса
- **POST** /items/{itemId}/comment - добавление отзыва о вещи
</details>
<details>
  <summary><h3>Запрос</h3></summary>
  
- **POST** /requests - добавление нового запроса на вещь
- **GET** /requests - просмотр всех своих запросов
- **GET** /requests/all - просмотр всех запросов
- **GET** /requests/{requestId} - просмотр запроса по ID и ответами на этот запрос
</details>
<details> 
 <summary><h3>Пользователь</h3></summary>
  
- **GET** /users - получение списка всех пользователей
- **GET** /users/{userId} - получение пользователя по ID
- **POST** /users - создание пользователя
- **PATCH** /users - обновление данных о пользователе
- **DELETE** /users/{userId} - удаление пользователя
</details>
<details> 
  <summary><h3>Бронирование</h3></summary>

- **POST** /bookings - добавление нового запроса на бронирование
- **PATCH** /bookings/{bookingId} - подтверждение или отклонение запроса на бронирование
- **GET** /bookings/{bookingId} - просмотр информации о бронировании по ID
- **GET** /bookings - просмотр всех бронирований текущего пользователя
- **GET** /bookings/owner - просмотр всех бронирований для всех вещей текущего пользователя
</details>

## Сборка и установка
Требования:
- Git
- JDK 11 или выше
- Maven 3.6.0 или выше
- Docker

Как запустить приложение:
1. Склонируйте репозиторий на локальную компьютер:
```bash
https://github.com/Anarmard/java-explore-with-me.git
```
2. Перейдите в директорию проекта:
```bash
cd java-explore-with-me
```
3. Соберите проект:
```bash
mvn clean install
```
4. Запустите приложение:
```bash
docker-compose up
```

## Стек технологий
- Java
- Spring Boot
- Spring Data JPA
- Mockito
- Apache Maven
- Docker
- Swagger
- Lombok (+ SLF4J)
- Postman


