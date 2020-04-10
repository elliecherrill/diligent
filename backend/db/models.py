from .database import DB

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


class Configuration:

    def __init__(self, title, creator, high, medium, low, courseCode, exerciseNum):
        self.title = title
        self.creator = creator
        self.high = high
        self.medium = medium
        self.low = low
        self.courseCode = courseCode
        self.exerciseNum = exerciseNum

    def save(self):
        """Save instance to DB"""
        return db.add("configuration", {
            "title": self.title,
            "creator": self.creator,
            "high": self.high,
            "medium": self.medium,
            "low": self.low,
            "courseCode": self.courseCode,
            "exerciseNum": self.exerciseNum
        })

    @classmethod
    def find_configs_by_username(cls, username):
        return db.find("configuration", {"creator": username})

    @classmethod
    def find_config_by_id(cls, config_id):
        return db.find_one("configuration", {"_id": config_id})

    @classmethod
    def delete_by_id(cls, config_id):
        return db.delete("configuration", {"_id": config_id})

    @classmethod
    def find_config_by_title(cls, username, title):
        return db.find("configuration", {"creator": username, "title": title})


