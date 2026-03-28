-- Добавление количества растений в заказ
ALTER TABLE "Order" ADD COLUMN quantity INT NOT NULL DEFAULT 1;
