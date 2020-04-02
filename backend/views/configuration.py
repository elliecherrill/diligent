import json
from urllib.parse import urlparse, urljoin

from bson import json_util, ObjectId
from flask import request, g, Blueprint, Response
from flask_jwt_extended import (
    jwt_required,
    get_jwt_identity
)
from flask_login import current_user

from backend import LOGIN_MANAGER
from ..db.models import User
from ..db.models import Configuration
import time

bp = Blueprint("configuration", __name__, url_prefix="/api/configuration")


def jsonify(obj):
    content = json.dumps(obj, default=json_util.default)
    return Response(content, 200, mimetype="application/json")


# =====================
#   REST API methods
# =====================
@bp.route("/server_time", methods=["GET"])
def get_server_time():
    timestamp_in_millis = int(time.time() * 1000)
    response = jsonify({"time": timestamp_in_millis})
    return response


@LOGIN_MANAGER.user_loader
def load_user(user_id):
    return User.find_by_id(user_id)


@bp.before_request
def get_current_user():
    g.user = current_user


@bp.route("/new_config", methods=["POST"])
@jwt_required
def create_new_quiz():
    json = request.json
    title = json["title"]
    creator = get_jwt_identity()
    high = json["high"]
    medium = json["medium"]
    low = json["low"]

    config = Configuration(title=title,
                           creator=creator,
                           high=high,
                           medium=medium,
                           low=low).save()

    response = jsonify({"id": str(config.inserted_id)})

    return response


##################################################################
# U T I L I T I E S
##################################################################
def is_safe_url(request_host_url, target):
    ref_url = urlparse(request_host_url)
    test_url = urlparse(urljoin(request_host_url, target))
    return test_url.scheme in ("http", "https") and ref_url.netloc == test_url.netloc
