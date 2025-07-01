-- Пользователи
CREATE TABLE IF NOT EXISTS "User" (
    id BIGSERIAL PRIMARY KEY,
--    role_id BIGINT NOT NULL REFERENCES Role(id) ON DELETE RESTRICT,
    role VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

-- Типы растений
CREATE TABLE IF NOT EXISTS PlantType (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- Сектора (уровневая структура)
CREATE TABLE IF NOT EXISTS Sector (
    id BIGSERIAL PRIMARY KEY,
    parent_id BIGINT REFERENCES Sector(id) ON DELETE SET NULL DEFERRABLE INITIALLY DEFERRED,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- Виды событий
CREATE TABLE IF NOT EXISTS EventType (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- Растения
CREATE TABLE IF NOT EXISTS Plant (
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
CREATE TABLE IF NOT EXISTS PlantHasType (
    plant_id BIGINT NOT NULL REFERENCES Plant(id),
    type_id BIGINT NOT NULL REFERENCES PlantType(id),
    type_value VARCHAR(255),
    PRIMARY KEY (plant_id, type_id)
);

-- Плантации в секторах
CREATE TABLE IF NOT EXISTS SectorPlant (
    id BIGSERIAL PRIMARY KEY,
    plant_id BIGINT NOT NULL REFERENCES Plant(id),
    plant_count INT NOT NULL CHECK (plant_count > 0),
    planting_date TIMESTAMP NOT NULL
);

-- События
CREATE TABLE IF NOT EXISTS Event (
    id BIGSERIAL PRIMARY KEY,
    type_id BIGINT NOT NULL REFERENCES EventType(id),
    commentary TEXT
);

-- Связь событий с плантациями
CREATE TABLE IF NOT EXISTS SectorEvent (
    id BIGSERIAL PRIMARY KEY,
    sector_plant_id BIGINT NOT NULL REFERENCES SectorPlant(id),
    event_id BIGINT NOT NULL REFERENCES Event(id),
    event_time TIMESTAMP NOT NULL
);

-- Заказы
CREATE TABLE IF NOT EXISTS "Order" (
    id BIGSERIAL PRIMARY KEY,
    company_name VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    commentary TEXT
);

-- Связь заказов с плантациями
CREATE TABLE IF NOT EXISTS SectorPlantHasOrder (
    sector_plant_id BIGINT NOT NULL REFERENCES SectorPlant(id),
    order_id BIGINT NOT NULL REFERENCES "Order"(id),
    PRIMARY KEY (sector_plant_id, order_id)
);

-- Индексы для оптимизации распространенных операций
CREATE INDEX IF NOT EXISTS idx_plant_spice ON Plant(spice);
CREATE INDEX IF NOT EXISTS idx_plant_sort ON Plant(sort);
CREATE INDEX IF NOT EXISTS idx_plant_type ON PlantHasType(plant_id, type_id);
CREATE INDEX IF NOT EXISTS idx_plant_event_time ON SectorEvent(event_time DESC);