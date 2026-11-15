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

const TYPE_INFO = {
  SOUP_KITCHEN: { icon: "🍲", label: "Comer" },
  SHELTER: { icon: "🛏️", label: "Dormir" },
  SHOWER: { icon: "🚿", label: "Ducha" },
  CLOTHING: { icon: "👕", label: "Ropa" },
};

const DAYS = [
  "SUNDAY",
  "MONDAY",
  "TUESDAY",
  "WEDNESDAY",
  "THURSDAY",
  "FRIDAY",
  "SATURDAY",
];

function todaySchedule(schedules) {
  const today = DAYS[new Date().getDay()];

  return schedules
    .filter((slot) => slot.dayOfWeek === today)
    .sort((a, b) => a.opensAt.localeCompare(b.opensAt))
    .map((slot) =>
      slot.opensAt === slot.closesAt
        ? "24 horas"
        : `${slot.opensAt.slice(0, 5)}–${slot.closesAt.slice(0, 5)}`,
    )
    .join(" y ");
}

function formatDate(isoString) {
  return new Date(isoString).toLocaleDateString("es-ES", {
    day: "numeric",
    month: "long",
    year: "numeric",
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
            <li key={place.id}>
              <h2>{place.name}</h2>
              <p>
                <span aria-hidden="true">{TYPE_INFO[place.type].icon}</span>{" "}
                {TYPE_INFO[place.type].label}
              </p>
              <p>{place.address}</p>
              <p>Hoy: {todaySchedule(place.schedules)}</p>
              <p>Actualizado el {formatDate(place.lastUpdated)}</p>
            </li>
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
