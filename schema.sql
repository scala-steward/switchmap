use switchmap;

CREATE TABLE IF NOT EXISTS switches (
	id SERIAL PRIMARY KEY,
    name STRING NOT NULL,
    ip INET NOT NULL,
    mac STRING NOT NULL,
    revision STRING NULL,
    serial STRING NULL,
    model STRING NOT NULL,
    build STRING NOT NULL,
    floor STRING NOT NULL,
    upswitch STRING NULL,
    port STRING NULL,
    postop STRING NOT NULL,
    posleft STRING NOT NULL
);

CREATE TABLE IF NOT EXISTS buildings (
	id SERIAL PRIMARY KEY,
	name STRING NOT NULL,
    addr STRING NOT NULL
);

CREATE TABLE IF NOT EXISTS floors (
	id SERIAL PRIMARY KEY,
	build STRING NOT NULL,
    floor STRING NOT NULL
);
