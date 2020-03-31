python3 -m venv venv
source venv/bin/activate
pip install --upgrade pip && pip install -r requirements.txt
source scripts/dev_exports.sh
flask run --no-reload