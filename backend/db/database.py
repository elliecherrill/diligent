from flask_pymongo import PyMongo


class Database:
    def __init__(self):
        self.mongo = PyMongo()

    def init_app(self, app):
        self.mongo.init_app(app)

    def delete(self, collection_name, query):
        self.mongo.db[collection_name].delete_one(query)

    def update(self, collection_name, query, new_values):
        self.mongo.db[collection_name].update_one(query, new_values)

    def add(self, collection_name, data):
        return self.mongo.db[collection_name].insert_one(data)

    def find_one(self, collection_name, query):
        return self.mongo.db[collection_name].find_one(query)

    def find(self, collection_name, query):
        return self.mongo.db[collection_name].find(query)

    def clear_db(self):
        db = self.mongo.db
        for collection in db.list_collection_names():
            db[collection].delete_many({})


DB = Database()
