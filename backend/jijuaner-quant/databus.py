from concurrent.futures import ProcessPoolExecutor, ThreadPoolExecutor
import nacos
import pymysql
from config import Config
from flask_pymongo import PyMongo
from flask_redis import FlaskRedis
from flask_sqlalchemy import SQLAlchemy

pymysql.install_as_MySQLdb()


redis = FlaskRedis()
db = SQLAlchemy()
mongo = PyMongo()
thread_pool = ThreadPoolExecutor()  # 任务量较少的IO密集型运算
process_pool = ProcessPoolExecutor()  # 用于CPU密集运算
nacos_client = nacos.NacosClient(
    server_addresses=Config.NACOS, namespace=Config.NAMESPACE)
nacos_client.add_naming_instance(
    Config.SERVER, Config.IP, Config.PORT, group_name=Config.GROUP, cluster_name=Config.CLUSTER)
