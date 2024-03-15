import './App.css'
import React, { useState } from 'react';
import Blur from 'react-blur';
import axios from 'axios';
import { FaCheck, FaTimes } from 'react-icons/fa';

function App() {
  const [image, setImage] = useState(null);
  const [faces, setFaces] = useState([]);
  const [isAppropriate, setIsAppropriate] = useState(true);
  const [loading, setLoading] = useState(false);
  const [file, setFile] = useState(null);
  const [imageInfo, setImageInfo] = useState({
    Violencia: 0,
    Adulto: 0,
    Spoof: 0,
    Medico: 0,
    Racy: 0,
    CantidadRostros: 0
  });

  const handleImageUpload = (e) => {
    const carga = e.target.files[0];
    if (carga) {
      setImage(URL.createObjectURL(carga));
    }
    setFile(carga);
    setFaces([]);
    setIsAppropriate(true);
  };
  
  const handleSendRequest = async () => {
    setLoading(true);
    if (file) {
      const formData = new FormData();
      formData.append('file', file);
      try {
        const response = await axios.post('http://localhost:8080/analyze', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        });
        const { Rostros, Resultado, ...info } = response.data;
        setFaces(Rostros);
        setIsAppropriate(Resultado === 'Imagen apropiada');
        setImageInfo(info);
      } catch (error) {
        console.error('Error:', error);
        // Aquí puedes mostrar un mensaje de error al usuario
      } finally {
        setLoading(false);
      }
    }
  };

  const renderValidIcon = () => {
    return isAppropriate ? <FaCheck style={{ color: 'green', marginRight: '5px' }} /> : <FaTimes style={{ color: 'red', marginRight: '5px' }} />;
  };

  const renderProgressBar = (percentage) => {
    const barColor = percentage > 50 ? 'red' : 'green';

    return (
      <div style={{ backgroundColor: '#ccc', height: '20px', width: '100%', borderRadius: '5px', marginTop: '5px' }}>
        <div style={{ backgroundColor: barColor, height: '100%', width: `${percentage}%`, borderRadius: '5px' }}></div>
      </div>
    );
  };

  return (
    <div>
      <h1>Detección de Rostros</h1>
      <input type="file" accept="image/*" onChange={handleImageUpload} />
      {image && (
        <div style={{ display: 'flex' }}>
          <div style={{ position: 'relative' }}>
            {isAppropriate ? (
              <img src={image} alt="Uploaded" />
            ) : (
              <img src={image} alt="Uploaded" style={{ filter: "blur(8px)" }}/>

            )}
            {faces.map((face, index) => (
              <div
                key={index}
                style={{
                  position: 'absolute',
                  border: '2px solid #00ff00',
                  left: face.Vertices[0].x,
                  top: face.Vertices[0].y,
                  width: face.Vertices[1].x - face.Vertices[0].x,
                  height: face.Vertices[2].y - face.Vertices[1].y
                }}
              ></div>
            ))}
          </div>
          <div style={{ marginLeft: '20px' }}>
            <div><h2>{renderValidIcon()}Imagen {isAppropriate ? 'válida' : 'no válida'}</h2></div>
            <div><h3>Cantidad de rostros: {imageInfo.CantidadRostros}</h3></div>
            <div><h2>Información de la imagen</h2></div>
            <div><h4>Violencia: {imageInfo.Violencia}% {renderProgressBar(imageInfo.Violencia)}</h4></div>
            <div><h4>Adulto: {imageInfo.Adulto}% {renderProgressBar(imageInfo.Adulto)}</h4></div>
            <div><h4>Parodia: {imageInfo.Spoof}% {renderProgressBar(imageInfo.Spoof)}</h4></div>
            <div><h4>Médico: {imageInfo.Medico}% {renderProgressBar(imageInfo.Medico)}</h4></div>
            <div><h4>Caliente: {imageInfo.Racy}% {renderProgressBar(imageInfo.Racy)}</h4></div>
          </div>
        </div>
      )}
      <button onClick={handleSendRequest} disabled={!image || loading}>
        {loading ? 'Cargando...' : 'Enviar'}
      </button>
    </div>
  );
}

export default App;
