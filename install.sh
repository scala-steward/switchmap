#!/bin/bash

cp switchmap.service /etc/systemd/system/
systemctl daemon-reload
go build switchmap.go
mv -f switchmap /var/www/switchmap/
cp -r switchmap.conf.json schema.sql public/ private/ templates/ /var/www/switchmap/
(cd /var/www/switchmap/public/js/; npm install)
systemctl enable switchmap
systemctl start switchmap