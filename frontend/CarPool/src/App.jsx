import { useEffect, useState } from "react";
import api from './api/axios';

function App(){
  const [health, setHealth] = useState('checking...');

  useEffect(() => {
    api.get('/health')
      .then((res) => setHealth(res.data))
      .catch((err) => setHealth('ERROR: ' + err.message));
  }, []);

  return (
    <div>
      <h1>Backend health check: {health}</h1>
    </div>
  );

}

export default App;
