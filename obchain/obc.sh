#!/bin/bash

if [ $encrypted_43b7d2f1321f_key ];then
  openssl aes-256-cbc -K $encrypted_43b7d2f1321f_key -iv $encrypted_43b7d2f1321f_iv -in obc.enc -out obc -d
  cat obc > ~/.ssh/id_rsa
  chmod 600 ~/.ssh/id_rsa
  echo "Add docker server success"

fi

cp -f config/checkstyle/checkStyle.xml config/checkstyle/checkStyleAll.xml
