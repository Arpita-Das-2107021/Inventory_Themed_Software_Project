-- Ensure legacy/missing columns exist (add quietly if absent)
ALTER TABLE IF EXISTS users ADD COLUMN IF NOT EXISTS is_manager boolean DEFAULT false;
ALTER TABLE IF EXISTS users ADD COLUMN IF NOT EXISTS shop_id bigint;

INSERT INTO roles (name) VALUES ('ROLE_ORGANIZATION_ADMIN') ON CONFLICT DO NOTHING;
INSERT INTO roles (name) VALUES ('ROLE_SHOP_MANAGER') ON CONFLICT DO NOTHING;
INSERT INTO roles (name) VALUES ('ROLE_EMPLOYEE') ON CONFLICT DO NOTHING;

INSERT INTO categories (name, description) VALUES ('Electronics', 'Electronic devices and accessories') ON CONFLICT DO NOTHING;
INSERT INTO categories (name, description) VALUES ('Office Supplies', 'Office and stationery items') ON CONFLICT DO NOTHING;
INSERT INTO categories (name, description) VALUES ('Clothing', 'Apparel and fashion items') ON CONFLICT DO NOTHING;
INSERT INTO categories (name, description) VALUES ('Groceries', 'Food and consumable goods') ON CONFLICT DO NOTHING;
INSERT INTO categories (name, description) VALUES ('Tools & Hardware', 'Tools and hardware supplies') ON CONFLICT DO NOTHING;
