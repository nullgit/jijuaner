from flask import Blueprint
from databus import db, thread_pool

fund_bp = Blueprint('fund', __name__, url_prefix='/fund')


class Fund(db.Model):
    __tablename__ = 'fund_list'
    fund_code = db.Column(db.String(10), primary_key=True)
    fund_name = db.Column(db.String(64))
    fund_type = db.Column(db.String(16))


@fund_bp.route('/<id>')
def fund(id: str):
    task = thread_pool.submit(Fund.query.get, (id,))
    fund = task.result()
    return str((fund.fund_code, fund.fund_name, fund.fund_type, 'hh'))
