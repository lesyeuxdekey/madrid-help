import { useEffect, useState } from "react";

function formatOpening(isoString) {
  const date = new Date(isoString);
  const time = date.toLocaleTimeString("es-ES", {
    hour: "2-digit",
    minute: "2-digit",
  });

  const today = new Date();
  const tomorrow = new Date();
  tomorrow.setDate(today.getDate() + 1);

  if (date.toDateString() === today.toDateString()) {
    return `hoy a las ${time}`;
  }
  if (date.toDateString() === tomorrow.toDateString()) {
    return `mañana a las ${time}`;
  }

  const weekday = date.toLocaleDateString("es-ES", { weekday: "long" });
  return `el ${weekday} a las ${time}`;
}

// NUEVO: pide una dirección y trata como error cualquier respuesta que no sea correcta
function getJson(url) {
  return fetch(url).then((response) => {
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`);
    }
    return response.json();
  });
}
function App() {
  const [places, setPlaces] = useState([]);
  const [next, setNext] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);
  const [attempt, setAttempt] = useState(0); // NUEVO

  useEffect(() => {
    getJson("/api/places?openNow=true")
      .then((data) => {
        setPlaces(data);
        if (data.length === 0) {
          return getJson("/api/places/next-opening").then((nextData) =>
            setNext(nextData),
          );
        }
      })
      .catch(() => setError(true))
      .finally(() => setLoading(false));
  }, [attempt]); // NUEVO: antes era []

  function retry() {
    // NUEVO
    setError(false);
    setLoading(true);
    setAttempt(attempt + 1);
  }

  if (loading) {
    return <p>Cargando...</p>;
  }

  if (error) {
    return (
      <main>
        {" "}
        {/* NUEVO: ahora lleva un botón */}
        <p role="alert">
          No se pudo cargar la información. Comprueba tu conexión e inténtalo de
          nuevo.
        </p>
        <button type="button" onClick={retry}>
          Reintentar
        </button>
      </main>
    );
  }

  return (
    <main>
      <h1>Madrid Help</h1>
      {places.length > 0 ? (
        <ul>
          {places.map((place) => (
            <li key={place.id}>{place.name}</li>
          ))}
        </ul>
      ) : (
        <>
          <p>Ahora no hay ningún lugar abierto.</p>
          {next && (
            <p>
              El próximo en abrir es {next.place.name},{" "}
              {formatOpening(next.opensAt)}.
            </p>
          )}
        </>
      )}
    </main>
  );
}

export default App;
