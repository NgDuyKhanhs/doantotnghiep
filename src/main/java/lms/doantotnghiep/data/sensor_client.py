import asyncio
import websockets
import json
import random

# Địa chỉ WebSocket của Spring Boot
WS_SERVER_URL = "ws://localhost:8080/ws"

async def send_sensor_data():
    try:
        async with websockets.connect(WS_SERVER_URL) as websocket:
            print("✅ Đã kết nối với WebSocket Server")

            while True:
                # Giả lập dữ liệu cảm biến
                data = {
                    "deviceId": "sensor_01",
                    "temperature": round(random.uniform(25, 50), 2),
                    "humidity": round(random.uniform(10, 90), 2),
                    "smoke": round(random.uniform(0, 500), 2)
                }

                # Chuyển dữ liệu thành JSON
                message = json.dumps(data)

                # Gửi dữ liệu đến WebSocket Server
                await websocket.send(message)
                print(f"📤 Đã gửi: {message}")

                # Chờ 3 giây trước khi gửi tiếp
                await asyncio.sleep(3)

    except Exception as e:
        print(f"❌ Lỗi kết nối WebSocket: {e}")

# Chạy chương trình
asyncio.run(send_sensor_data())