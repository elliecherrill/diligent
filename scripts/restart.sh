#! /bin/bash

export PATH=$PWD/venv/bin:$PATH
export diligent_path=$PWD

cd ~
rm -rf ~/individual_project
cp -r $diligent_path ~/individual_project
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

killall node

cd frontend/
npx serve -s build -l 3000 &>/dev/null &