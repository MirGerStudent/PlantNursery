-- Добавление новых секторов
INSERT INTO Sector (parent_id, name) VALUES
(1, 'Школа 1'),
(4, 'Поле 1'), (4, 'Поле 2'), (4, 'Поле 3'),
(4, 'Поле 4'), (4, 'Поле 5'), (4, 'Поле 6'),
(4, 'Поле 7'), (4, 'Поле 8'), (4, 'Поле 9');

-- Добавление нового растения
INSERT INTO Plant (height_stem_min, height_stem_max, width_stem_min, width_stem_max, spice, sort, description) VALUES
(2, 18, 1, 4, 'Ива', 'Ива обыкновенная', 'Самая крутая обыкновенная ива'),
(1, 3, 1, 3, 'Древко', 'Древко обыкновенное', 'Самое крутое обыкновенное древко');

-- Добавление растений на поля
INSERT INTO SectorPlant (id, plant_id, plant_count, planting_date) VALUES
(5, 2, 130, NOW()),
(6, 2, 130, NOW()),
(7, 3, 130, NOW()),
(8, 3, 130, NOW());

-- Добавление пользователей
INSERT INTO "User" (role, name, password) VALUES
('ADMIN', 'ADMIN', 'password'),
('WORKER', 'WORKER', 'password');