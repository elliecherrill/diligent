from urllib.parse import urlparse, urljoin

import ldap
import json
from bson import json_util
from flask import request, g, Blueprint, Response
from flask_jwt_extended import (
    create_access_token,
    get_jwt_identity,
    jwt_refresh_token_required,
    jwt_required,
)
from flask_login import current_user

from backend import (
    LOGIN_MANAGER,
)
from ..auth import auth
from ..auth import ldap_constants
from ..db.models import User

bp = Blueprint("home", __name__, url_prefix="/api/")

def jsonify(obj):
    content = json.dumps(obj, default=json_util.default)
    return Response(content, 200, mimetype="application/json")

@LOGIN_MANAGER.user_loader
def load_user(user_id):
    return User.find_by_id(user_id)

@bp.before_request
def get_current_user():
    g.user = current_user


##################################################################
# U T I L I T I E S
##################################################################
def is_safe_url(request_host_url, target):
    ref_url = urlparse(request_host_url)
    test_url = urlparse(urljoin(request_host_url, target))
    return test_url.scheme in ("http", "https") and ref_url.netloc == test_url.netloc
