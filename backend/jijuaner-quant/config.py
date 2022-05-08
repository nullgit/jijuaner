class Config:
    # flask
    HOST = 'localhost'
    PORT = 9090
    FLASK_CONFIG = {
        'SQLALCHEMY_DATABASE_URI': 'mysql://root:root@localhost:3306/jijuaner_fund',
        'SQLALCHEMY_TRACK_MODIFICATIONS': False,
        'REDIS_URL': 'redis://localhost:6379/0',
        'MONGO_URI': 'mongodb://localhost:27017/jijuaner_quant'
    }

    # nacos
    NACOS = 'http://localhost:8848'
    SERVER = 'quant'
    NAMESPACE = '0686879d-ea4f-45b0-ad9a-9da5681d2420'
    GROUP = 'dev'
    IP = 'localhost'
    CLUSTER = 'DEFAULT'


