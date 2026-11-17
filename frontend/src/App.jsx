import {
  BedDouble,
  Clock,
  MapPin,
  Shirt,
  ShowerHead,
  Soup,
} from "lucide-react";
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
  SOUP_KITCHEN: { Icon: Soup, label: "Comer" },
  SHELTER: { Icon: BedDouble, label: "Dormir" },
  SHOWER: { Icon: ShowerHead, label: "Ducha" },
  CLOTHING: { Icon: Shirt, label: "Ropa" },
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

const FILTERS = [
  { value: "", Icon: MapPin, label: "Todo" },
  { value: "SOUP_KITCHEN", Icon: Soup, label: "Comer" },
  { value: "SHELTER", Icon: BedDouble, label: "Dormir" },
  { value: "SHOWER", Icon: ShowerHead, label: "Ducha" },
  { value: "CLOTHING", Icon: Shirt, label: "Ropa" },
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

function TypeIcon({ type }) {
  const Icon = TYPE_INFO[type].Icon;
  return <Icon size={16} aria-hidden="true" />;
}

function DirectionsLink({ place }) {
  return (
    <a
      className="directions"
      href={`https://www.google.com/maps/dir/?api=1&destination=${place.latitude},${place.longitude}&travelmode=walking`}
      target="_blank"
      rel="noopener noreferrer"
      aria-label={`Cómo llegar en Google Maps: ${place.name}`}
    >
      Cómo llegar en Google Maps <span aria-hidden="true">↗</span>
    </a>
  );
}

function App() {
  const [places, setPlaces] = useState([]);
  const [next, setNext] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);
  const [attempt, setAttempt] = useState(0);
  const [selectedType, setSelectedType] = useState("");

  useEffect(() => {
    let ignore = false;
    const typeQuery = selectedType ? `type=${selectedType}` : "";

    getJson(`/api/places?openNow=true&${typeQuery}`)
      .then((data) => {
        if (ignore) return;
        setPlaces(data);
        if (data.length === 0) {
          return getJson(`/api/places/next-opening?${typeQuery}`).then(
            (nextData) => {
              if (!ignore) {
                setNext(nextData);
              }
            },
          );
        }
      })
      .catch(() => {
        if (!ignore) {
          setError(true);
        }
      })
      .finally(() => {
        if (!ignore) {
          setLoading(false);
        }
      });

    return () => {
      ignore = true;
    };
  }, [attempt, selectedType]);

  function retry() {
    setError(false);
    setLoading(true);
    setAttempt(attempt + 1);
  }

  function selectType(type) {
    setError(false);
    setLoading(true);
    setSelectedType(type);
  }

  let content; // NUEVO: aquí guardamos lo que se va a mostrar

  if (loading) {
    content = <p>Cargando...</p>;
  } else if (error) {
    content = (
      <>
        <p role="alert">
          No se pudo cargar la información. Comprueba tu conexión e inténtalo de
          nuevo.
        </p>
        <button type="button" onClick={retry}>
          Reintentar
        </button>
      </>
    );
  } else if (places.length > 0) {
    content = (
      <ul>
        {places.map((place) => (
          <li key={place.id}>
            <div className="card-top">
              <span className="badge badge-type">
                <TypeIcon type={place.type} />
                {TYPE_INFO[place.type].label}
              </span>
              <span className="badge badge-open">Abierto ahora</span>
            </div>
            <h2>{place.name}</h2>
            <p>{place.address}</p>
            <p className="hours">
              <span className="hours-label">Hoy</span>{" "}
              {todaySchedule(place.schedules)}
            </p>
            <DirectionsLink place={place} />
            <p className="updated">
              Actualizado el {formatDate(place.lastUpdated)}
            </p>
          </li>
        ))}
      </ul>
    );
  } else {
    content = (
      <section className="empty">
        <Clock size={32} aria-hidden="true" />
        <h2>Ahora no hay ningún lugar abierto</h2>
        {next && (
          <div className="next">
            <span className="badge badge-soon">
              Abre {formatOpening(next.opensAt)}
            </span>
            <h3>{next.place.name}</h3>
            <p>{next.place.address}</p>
            <DirectionsLink place={next.place} />
          </div>
        )}
      </section>
    );
  }

  return (
    <>
      <header>
        <div className="header-inner">
          <img src="/logo.svg" alt="" width="48" height="48" />
          <div>
            <h1>Cerca Madrid</h1>
            <p>Ayuda abierta ahora en Madrid</p>
          </div>
        </div>
      </header>
      <main>
        <div role="group" aria-label="Tipo de ayuda">
          {FILTERS.map((filter) => (
            <button
              key={filter.value}
              type="button"
              aria-pressed={selectedType === filter.value}
              onClick={() => selectType(filter.value)}
            >
              {selectedType === filter.value && (
                <span aria-hidden="true">✓ </span>
              )}
              <filter.Icon size={18} aria-hidden="true" />
              {filter.label}{" "}
            </button>
          ))}
        </div>
        {content}
      </main>
    </>
  );
}

export default App;
