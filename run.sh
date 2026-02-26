#!/bin/bash

export DB_PASSWORD=ITacademy2026
export JWT_SECRET=T0k4m3M4ss4g3S3cr3tK3y2026!XyZ#mNpQrStUvWxAbCdEfGhIjKlMnOpQrStUv
export SENDGRID_API_KEY=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
export SENDGRID_FROM_EMAIL=tokamemassage@gmail.com
export SENDGRID_FROM_NAME=Tokame Massage

./mvnw spring-boot:run -Dmaven.test.skip=true

