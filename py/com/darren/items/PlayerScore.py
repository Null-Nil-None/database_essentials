from pydantic import BaseModel

class PlayerScore(BaseModel):
    player_name: str
    score: int