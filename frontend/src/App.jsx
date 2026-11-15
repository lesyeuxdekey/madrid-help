import { useEffect, useState } from "react";

function App() {
  const [places, setPlaces] = useState([]);

  useEffect(() => {
    fetch("/api/places")
      .then((response) => response.json())
      .then((data) => setPlaces(data));
  }, []);

  return (
    <main>
      <h1>Madrid Help</h1>
      <p>La API respondió con {places.length} lugares:</p>
      <ul>
        {places.map((place) => (
          <li key={place.id}>
            {place.name} ({place.type})
          </li>
        ))}
      </ul>
    </main>
  );
}

export default App;
