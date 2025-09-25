# Traceability Matrix

Документ связывает требования проекта с задачами (issues) и коммитами для обеспечения прозрачности выполнения.

---

## 1. Functional Requirements → Issues → Commits

| Требование | Issue | Commit |
|------------|-------|--------|
| FR-1: Настроить Spring Boot проект | Настроить Spring Boot проект | a1b2c3 |
| FR-2: Подключить БД H2 | Подключить БД H2 | d4e5f6 |
| FR-3: Реализовать модель UserFinancials | Реализовать модель UserFinancials | g7h8i9 |
| FR-4: Написать SQL-запрос для UserFinancials | Написать SQL-запрос для UserFinancials | j1k2l3 |
| FR-5: Реализовать интерфейс RecommendationRuleSet | Реализовать интерфейс RecommendationRuleSet | m4n5o6 |
| FR-6: Добавить правила Invest500Rule / SimpleCreditRule / TopSavingRule | Добавить правило Invest500Rule<br>Добавить правило SimpleCreditRule<br>Добавить правило TopSavingRule | p7q8r9 |
| FR-7: Реализовать RecommendationService | Реализовать RecommendationService | s1t2u3 |
| FR-8: Реализовать RecommendationController | Реализовать RecommendationController | v4w5x6 |
| FR-9: Подключить Swagger для API | Подключить Swagger | y7z8a9 |

---

## 2. Non-Functional Requirements → Issues → Commits

| Требование | Issue | Commit |
|------------|-------|--------|
| NFR-1: Покрытие правил unit-тестами | Написать unit-тесты для правил | b1c2d3 |
| NFR-2: Проверка SQL-запросов | Протестировать SQL-запрос в БД | e4f5g6 |
| NFR-3: Документирование проекта | Написать документацию в Wiki | h7i8j9 |
| NFR-4: Диаграммы проекта | Нарисовать диаграмму компонентов<br>Нарисовать диаграмму активности<br>Нарисовать диаграмму развертывания | k1l2m3 |

---

## 3. User Story → Issues → Commits

**User Story:**
> Как пользователь, я хочу получить персональные рекомендации, чтобы улучшить свой финансовый портфель.

| User Story | Issue | Commit |
|------------|-------|--------|
| Получение персональных рекомендаций | Добавить правило Invest500Rule<br>Добавить правило SimpleCreditRule<br>Добавить правило TopSavingRule | p7q8r9 |
| Получение рекомендаций через REST API | Реализовать RecommendationController | v4w5x6 |
| Просмотр документации API | Подключить Swagger | y7z8a9 |

---

## 4. Deploy Requirements → Issues → Commits

| Требование | Issue | Commit |
|------------|-------|--------|
| DR-1: Документация по развертыванию | Добавить требования к развертыванию | n1o2p3 |

---

## 5. Management

| Действие | Issue | Commit |
|-----------|-------|--------|
| Настройка Kanban | Настроить GitHub Project (Kanban) | q4r5s6 |
| Финальная проверка проекта | Финальная проверка проекта | t7u8v9 |

---
