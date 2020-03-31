from .database import DB
from bson import ObjectId

db = DB

class User:

    def __init__(self, username, firstname, surname, is_student):
        self.username = username
        self.firstname = firstname
        self.surname = surname
        self.is_student = is_student

    def get_id(self):
        return self.username

    @property
    def is_authenticated(self):
        """Authentication handled by LDAP"""
        return True

    @property
    def is_anonymous(self):
        """Anonymous users not supported"""
        return False

    @property
    def is_active(self):
        """All users are active"""
        return True

    def save(self):
        """Save instance to DB"""
        db.add("user", {
            "username": self.username,
            "firstname": self.firstname,
            "surname": self.surname,
            "is_student": self.is_student
        })

    @classmethod
    def find_by_id(cls, _id):
        return db.find_one(cls, "user", {"_id": _id})

    @classmethod
    def find_by_username(cls, username):
        return db.find_one("user", {"username": username})

# TODO
# CREATE A CLASS CALLED CONFIGURATION - see veracty file





