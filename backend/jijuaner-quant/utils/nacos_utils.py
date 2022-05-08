from databus import nacos_client
from config import Config
from time import sleep
import aiohttp


async def send_request_to_nacos_instance(service_name: str, url: str) -> str:
    '''
    向nacos中的某个服务发送请求
    '''
    server = nacos_client.list_naming_instance(
        service_name, namespace_id=Config.NAMESPACE, group_name=Config.GROUP)['hosts'][0]
    async with aiohttp.ClientSession() as session:
        async with session.get(f'http://{server["ip"]}:{server["port"]}{url}') as resp:
            return await resp.json()


def send_heartbeat() -> None:
    print('send')
    while True:
        nacos_client.send_heartbeat(
            Config.SERVER, Config.IP, Config.PORT, group_name=Config.GROUP, cluster_name=Config.CLUSTER)
        sleep(5)
