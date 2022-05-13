from bp.index import index_bp
from bp.bond import bond_bp
from utils.nacos_utils import send_heartbeat, send_request_to_nacos_instance
from gevent import pywsgi
from flask import Flask
from databus import db, mongo, process_pool, redis, thread_pool
from config import Config
import asyncio
import argparse
import os

os.chdir('/home/ubuntu/project/jijuaner/backend/jijuaner-quant/')


parser = argparse.ArgumentParser(description='jijuaner-quant 鸡圈儿量化模块')
parser.add_argument('--debug', type=bool, default=False,
                    metavar='DEBUG', help='flask是否开启debug模式')
args = parser.parse_args()

app = Flask(__name__)
for k, v in Config.FLASK_CONFIG.items():
    app.config[k] = v
redis.init_app(app)
db.init_app(app)
mongo.init_app(app)


app.register_blueprint(index_bp)
app.register_blueprint(bond_bp)


@app.route('/', methods=['GET', 'POST'])
def index():
    return '你好'


@app.route('/test')
def test() -> str:
    # return asyncio.run(send_request_to_nacos_instance('fund', '/fund/fundInfo/simple/110011'))
    from quant.risk_premium import back_test_risk_premium_strategy, get_fig

    # get_fig('沪深300', '中国国债收益率10年')
    # get_fig('中证800', '中国国债收益率10年')
    back_test_risk_premium_strategy('110011')
    return 'ok'


if __name__ == '__main__':
    thread_pool.submit(send_heartbeat)
    # app.run(host=Config.HOST, port=Config.PORT)
    # app.run(host=Config.HOST, port=Config.PORT, debug=True)
    app.run(host=Config.HOST, port=Config.PORT, debug=args.debug)

    # 生产环境
    # server = pywsgi.WSGIServer((HOST, PORT), app)
    # server.serve_forever()
