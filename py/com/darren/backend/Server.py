from __future__ import annotations
from typing import Optional
import warnings

from fastapi import FastAPI, File, UploadFile, HTTPException
import motor.motor_asyncio
from motor.motor_asyncio import AsyncIOMotorClient, AsyncIOMotorDatabase

from com.darren.items import PlayerScore

app: FastAPI = FastAPI()

class Server:
    _inst: 'Server' = None
    
    def __init__(self, username: str, password: str):
        if Server._inst is not None:
            raise RuntimeError(f"Only a single instance of {type(self).__name__} is allowed to exist during runtime.")
        
        self._username = username
        self._password = password
        
        self_client: AsyncIOMotorClient = AsyncIOMotorClient(f"mongodb+srv://{username}:{password}@cluster0.znflm.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0")
        self._db: AsyncIOMotorDatabase = self._client.multimedia_db
        
        Server._inst = self
        
    @app.post("/upload_sprite")
    async def upload_sprite(self, file: UploadFile = File(...)):
    # In a real application, the file should be saved to a storage service
        content = await file.read()
        sprite_doc = {"filename": file.filename, "content": content}
        result = await self._db.sprites.insert_one(sprite_doc)
        return {"message": "Sprite uploaded", "id": str(result.inserted_id)}

    @app.post("/upload_audio")
    async def upload_audio(self, file: UploadFile = File(...)):
        content = await file.read()
        audio_doc = {"filename": file.filename, "content": content}
        result = await self._db.audio.insert_one(audio_doc)
        
        return {"message": "Audio file uploaded", "id": str(result.inserted_id)}

    @app.post("/player_score")
    async def add_score(self, score: PlayerScore):
        score_doc = score.dict()
        result = await self._db.scores.insert_one(score_doc)
        
        return {"message": "Score recorded", "id": str(result.inserted_id)}
    
    def getInst(username: Optional[str], password: Optional[str]) -> Server:
        if Server._inst is None:
            Server._inst = Server(username, password)
            
        elif username is not None or password is not None:
            warnings.warn(f"An call to Sever.getInst() was made with a supplied username and password. However, these will have no effect, as the singleton is already initalised.")
            
        return Server._inst


