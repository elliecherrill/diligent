#! /bin/bash

echo Starting restart script

export PATH=$PWD/venv/bin:$PATH
export diligent_path=$PWD

echo here 1

cd ~

echo here 2

rm -rf ~/individual_project

echo here 3

cp -r $diligent_path ~/individual_project

echo here 4

cd individual_project

echo Killing any gunicorn instances
pkill gunicorn
echo Starting gunicorn
gunicorn backend.wsgi:app \
           --worker-class eventlet \
           --workers 1 \
           --bind 0.0.0.0:5000 \
           --timeout 500\
           --log-file $HOME/backend.log \
           --log-level debug \
           --capture-output \
           --enable-stdio-inheritance \
           --daemon

if ! pgrep -x "node" > /dev/null
then
  echo Node not running already
else
  echo Killing and restarting node
  sudo killall node
fi

serve -s frontend/build -l 3000 &>/dev/null &