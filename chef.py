
from langchain.messages import HumanMessage
from langchain_community.tools import TavilySearchResults
from typing import Any, Dict
from dotenv import load_dotenv
from langchain.tools import tool
from langchain_openai import ChatOpenAI
import os
import base64
from langchain.agents import create_agent
from tavily import TavilyClient
from langgraph.checkpoint.memory import InMemorySaver  
load_dotenv()
tavily_client = TavilyClient()
model = ChatOpenAI(model="qwen3-vl-plus",base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",temperature=0.7)
agent = create_agent(model,tools=[TavilySearchResults(max_results=3)])
class Chef:
    tavily_client = TavilyClient()
    model = ChatOpenAI(model="qwen3-vl-plus",base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",temperature=0.7)
    agent = create_agent(model,tools=[TavilySearchResults(max_results=3)])
    @staticmethod
    def encode_image(image_path):
        with open(image_path, "rb") as image_file:
            return base64.b64encode(image_file.read()).decode("utf-8")
    @staticmethod 
    def chat(query: str,img_path: str) :
        """Chat with the agent"""
        base64_image = Chef.encode_image(img_path)
        config = {"configurable": {"thread_id": "1"}}
        multimodal_question = HumanMessage(content=[
    {"type": "text", "text": query},
    {"type": "image", "base64": base64_image, "mime_type": "image/png"}
        ])
        flag = False
        for token, metadata in Chef.agent.stream(
            {"messages": [multimodal_question]},
            config, 
            stream_mode="messages"
        ):
            if metadata['langgraph_node'] == 'model' and token.content_blocks and token.content_blocks[0]['type'] == 'text':
                if not flag:
                    print("模型回答：")
                flag = True
                print(token.content_blocks[0]['text'],end='',flush=True)
                
        print("\n")
正在处理的内容


        
    
        
        

        