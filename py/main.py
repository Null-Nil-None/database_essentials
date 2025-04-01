from fastapi import FastAPI, File, UploadFile, HTTPException
import motor.motor_asyncio

from com.darren.items.PlayerScore import PlayerScore
from com.darren.backend.Server import Server

server: Server = Server.getInst(username="MilshakeSuchomimus", password="inthegalleryofherosandlizards")


# app = FastAPI()

# # Initialize MongoDB client
# client = motor.motor_asyncio.AsyncIOMotorClient("mongodb+srv://MilshakeSuchomimus:inthegalleryofherosandlizards@cluster0.znflm.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0")
# db = client.your_database_name  # Replace 'your_database_name' with your actual database name

# @app.get("/test_connection")
# async def test_connection():
#     try:
#         # Attempt to find one document in your collection (replace 'test' with your actual collection name)
#         document = await db.test.find_one()
#         return {"status": "success", "data": document}
#     except Exception as e:
#         return {"status": "error", "message": str(e)}

# Example from Atlas
# from pymongo.mongo_client import MongoClient
# from pymongo.server_api import ServerApi
# uri = "mongodb+srv://<db_username>:<db_password>@cluster0.znflm.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0"
# # Create a new client and connect to the server
# client = MongoClient(uri, server_api=ServerApi('1'))
# # Send a ping to confirm a successful connection
# try:
#     client.admin.command('ping')
#     print("Pinged your deployment. You successfully connected to MongoDB!")
# except Exception as e:
#     print(e)