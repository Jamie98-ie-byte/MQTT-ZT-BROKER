MQTT-ZT Secure Broker
A lightweight, secure, and extensible MQTT broker designed to implement Zero Trust principles for IoT environments. This project integrates fine-grained access control using Attribute-Based Access Control (ABAC) model with continuous enforcement via XACML.

📌 Features
✅ Lightweight MQTT Broker (based on Moquette)

🔐 Attribute-Based Access Control (ABAC)

📜 XACML Policy Enforcement via Axiomatics PEP SDK (Java)

⛔ Dynamic access revocation on policy violation

🔍 Real-time request evaluation and decision-making

🔒 Zero Trust Architecture (ZTA) principles applied

🏗️ Architecture Overview
+----------+       +--------+       +--------+       +--------+
|  Broker  |   +   |  PEP   | <---> |  PDP   | <---> | Policy |
+----------+       +--------+       +--------+       +--------+
     |                  |                |                |
     |              Intercepts      checks policies   ALFA engine 
     |              MQTT traffic    and sends its     where the policies and 
     |              sends requests  decision to PEP   attributes are created and 
     |              for decisions   for enforcement   stored.
     |              and enforces    after checking 
     |              it through      the policies.
     |              broker. 
     V
  [Moquette Broker]
