-- Роли пользователей
CREATE TABLE Role (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- Пользователи
CREATE TABLE "User" (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL REFERENCES Role(id) ON DELETE RESTRICT,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL
);

-- Типы растений
CREATE TABLE PlantType (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- Сектора (уровневая структура)
CREATE TABLE Sector (
    id BIGSERIAL PRIMARY KEY,
    parent_id BIGINT REFERENCES Sector(id) ON DELETE SET NULL DEFERRABLE INITIALLY DEFERRED,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- Виды событий
CREATE TABLE EventType (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- Растения
CREATE TABLE Plant (
    id BIGSERIAL PRIMARY KEY,
    height_stem_min INT CHECK (height_stem_min >= 0),
    height_stem_max INT CHECK (height_stem_max >= 0),
    width_stem_min INT CHECK (width_stem_min >= 0),
    width_stem_max INT CHECK (width_stem_max >= 0),
    spice VARCHAR(255) NOT NULL,
    sort VARCHAR(255) NOT NULL,
    description TEXT
);

-- Связь растений с типами
CREATE TABLE PlantHasType (
    plant_id BIGINT NOT NULL REFERENCES Plant(id),
    type_id BIGINT NOT NULL REFERENCES PlantType(id),
    type_value VARCHAR(255),
    PRIMARY KEY (plant_id, type_id)
);

-- Плантации в секторах
CREATE TABLE SectorPlant (
    id BIGSERIAL PRIMARY KEY,
    plant_id BIGINT NOT NULL REFERENCES Plant(id),
    plant_count INT NOT NULL CHECK (plant_count > 0),
    planting_date TIMESTAMP NOT NULL
);

-- События
CREATE TABLE Event (
    id BIGSERIAL PRIMARY KEY,
    type_id BIGINT NOT NULL REFERENCES EventType(id),
    commentary TEXT
);

-- Связь событий с плантациями
CREATE TABLE SectorEvent (
    id BIGSERIAL PRIMARY KEY,
    sector_plant_id BIGINT NOT NULL REFERENCES SectorPlant(id),
    event_id BIGINT NOT NULL REFERENCES Event(id),
    event_time TIMESTAMP NOT NULL
);

-- Заказы
CREATE TABLE "Order" (
    id BIGSERIAL PRIMARY KEY,
    company_name VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    commentary TEXT
);

-- Связь заказов с плантациями
CREATE TABLE SectorPlantHasOrder (
    sector_plant_id BIGINT NOT NULL REFERENCES SectorPlant(id),
    order_id BIGINT NOT NULL REFERENCES "Order"(id),
    PRIMARY KEY (sector_plant_id, order_id)
);

-- Индексы для оптимизации распространенных операций
CREATE INDEX idx_plant_spice ON Plant(spice);
CREATE INDEX idx_plant_sort ON Plant(sort);
CREATE INDEX idx_plant_type ON PlantHasType(plant_id, type_id);
CREATE INDEX idx_plant_event_time ON SectorEvent(event_time DESC);