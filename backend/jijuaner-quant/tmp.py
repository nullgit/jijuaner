import asyncio
import time

async def f(name):
    await asyncio.sleep(1)
    print(f'hello, {name}')

async def main():
    tasks = [
        asyncio.create_task(f('a')),
        asyncio.create_task(f('b')),
        asyncio.create_task(f('c')),
    ]
    await asyncio.wait(tasks)

if __name__ == '__main__':
    start = time.time()
    asyncio.run(main())
    print(time.time() - start)
