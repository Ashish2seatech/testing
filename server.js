const express = require('express');
const http = require('http');

const app = express();
const server = http.createServer(app);

const PORT = process.env.PORT || 3000;


app.get('/api/v1/test', (request, response, next) => {
    response.send({ success: true });
});

server.listen(PORT, () => {
    console.log(`Server started at port: ${PORT}`);
});