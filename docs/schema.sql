CREATE TABLE IF NOT EXISTS builds
(
    name       STRING NOT NULL UNIQUE,
    short_name STRING NOT NULL UNIQUE,
    PRIMARY KEY (name, short_name)
);

CREATE TABLE IF NOT EXISTS floors
(
    number           INT    NOT NULL,
    build_name       STRING NOT NULL,
    build_short_name STRING NOT NULL,
    PRIMARY KEY (build_short_name, number),
    CONSTRAINT fk_build FOREIGN KEY (build_name, build_short_name) REFERENCES builds (name, short_name)
);

CREATE TABLE IF NOT EXISTS switches
(
    name             STRING     NOT NULL UNIQUE,
    ip               INET       NULL UNIQUE,
    mac              STRING(12) NOT NULL UNIQUE,
    revision         STRING     NULL,
    serial           STRING     NULL UNIQUE,
    ports_number     INT        NULL,
    build_short_name STRING     NULL,
    floor_number     INT        NULL,
    position_top     INT        NULL,
    position_left    INT        NULL,
    up_switch_name   STRING     NULL,
    up_switch_mac    STRING(12) NULL,
    up_link          STRING     NULL,
    PRIMARY KEY (name, mac),
    CONSTRAINT fk_floor FOREIGN KEY (build_short_name, floor_number) REFERENCES floors (build_short_name, number),
    CONSTRAINT fk_switch FOREIGN KEY (up_switch_name, up_switch_mac) REFERENCES switches (name, mac)
);