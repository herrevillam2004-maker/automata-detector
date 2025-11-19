const detectBtn = document.getElementById("detectBtn");
const clearBtn = document.getElementById("clearBtn");
const inputText = document.getElementById("inputText");
const usersList = document.getElementById("usersList");
const hashtagsList = document.getElementById("hashtagsList");
const urlsList = document.getElementById("urlsList");
const graphArea = document.getElementById("graphArea");
const simInput = document.getElementById("simInput");
const stepBtn = document.getElementById("stepBtn");
const runBtn = document.getElementById("runBtn");
const simOutput = document.getElementById("simOutput");

let automataDots = {};

detectBtn.addEventListener("click", async () => {
  const text = inputText.value.trim();
  if (!text) return;

  try {
     graphArea.innerHTML = `En proceso...`;
    // Consumir nuevo endpoint /api/analizar
    const resp = await fetch("http://localhost:8080/api/extraer", {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ texto: text })
    });

    if (!resp.ok) throw new Error("Error al consumir /api/extraer: " + resp.statusText);
    const json = await resp.json();

    // Extraer detecciones
    const detections = {
      users: json.menciones || [],
      hashtags: json.hashtags || [],
      urls: json.urls || []
  
    };

    automataDots = json.automata || {};

    // Rellenar las listas como antes
    fillList(usersList, detections.users);
    fillList(hashtagsList, detections.hashtags);
    fillList(urlsList, detections.urls);

    graphArea.innerHTML = `CARGANDO...`;
    // Opcional: obtener imagen del autómata
    const imgResp = await fetch("http://localhost:8080/api/analizar");
    if (imgResp.ok) {
      const blob = await imgResp.blob();
      const imgUrl = URL.createObjectURL(blob);
      // Aquí podrías mostrar la imagen en algún div si quieres---max-width:50vw
      graphArea.innerHTML = `<img src="${imgUrl}" style=" max-height:50vh; width:auto; height:auto; display:block; margin:0 auto;" />`;
      console.log("Imagen del autómata cargada: " + imgUrl);
    }else{
      alert("Imagen del autómata no disponible");
    }

  } catch (error) {
    console.error(error);
    alert("Ocurrió un error al consumir los endpoints" + error.message);
  }
});


clearBtn.addEventListener("click", () => {
  inputText.value = "";
  fillList(usersList, []);
  fillList(hashtagsList, []);
  fillList(urlsList, []);
  graphArea.innerHTML = "";
  simOutput.innerHTML = "";
});

function fillList(ul, arr) {
  ul.innerHTML = "";
  if (!arr || arr.length === 0) {
    ul.innerHTML = "<li>—</li>";
    return;
  }
  arr.forEach(item => {
    const li = document.createElement("li");
    li.textContent = item;
    ul.appendChild(li);
  });
}

document.querySelectorAll(".showAuto").forEach(btn => {
  btn.addEventListener("click", () => {
    const which = btn.getAttribute("data-which");
    const dot = automataDots[which];
    if (!dot) {
      graphArea.innerHTML = "<p style='color:#777'>Primero presiona 'Detectar patrones' para cargar vista desde el automata.</p>";
      return;
    }
    try {
      const viz = new window.Viz();
      viz.renderSVGElement(dot)
         .then(element => {
           graphArea.innerHTML = "";
           graphArea.appendChild(element);
         })
         .catch(e => {
           graphArea.innerHTML = "<pre>Error al renderizar: " + e + "</pre>";
         });
    } catch (e) {
      graphArea.innerHTML = "<pre>Viz.js no cargado: " + e + "</pre>";
    }
  });
});

function isLetterDigitOrUnderscore(ch) {
  return /[A-Za-z0-9_]/.test(ch);
}

function simulateDFA(input) {
  if (!input || input.length === 0) return {accepted: false, reason: "Cadena vacía"};
  const first = input.charAt(0);
  let startState = "q0";
  if (first === "#") return simulateHash(input);
  if (first === "@") return simulateUser(input);
  return {accepted:false, reason:"Cadena no comienza con '@' ni '#'"};
}

function simulateHash(s) {
  let state = "q0";
  let i = 0;
  const steps = [];
  while (i < s.length) {
    const ch = s.charAt(i);
    if (state === "q0") {
      steps.push(`estado q0, leyendo '${ch}'`);
      if (ch === '#') { state = "q1"; i++; }
      else { return {accepted:false, reason:"q0 esperaba #", steps}; }
    } else if (state === "q1") {
      steps.push(`estado q1, leyendo '${ch}'`);
      if (isLetterDigitOrUnderscore(ch)) { state = "q2"; i++; }
      else { return {accepted:false, reason:"q1 esperaba letra/dígito/_", steps}; }
    } else if (state === "q2") {
      steps.push(`estado q2, leyendo '${ch}'`);
      if (isLetterDigitOrUnderscore(ch)) { i++; }
      else { return {accepted:false, reason:"q2 esperaba letra/dígito/_ o fin", steps}; }
    }
  }
  const accepted = (state === "q2");
  return {accepted, reason: accepted ? "Cadena aceptada" : "Cadena no aceptada", steps};
}

function simulateUser(s) {
  let state = "q0";
  let i = 0;
  const steps = [];
  while (i < s.length) {
    const ch = s.charAt(i);
    if (state === "q0") {
      steps.push(`estado q0, leyendo '${ch}'`);
      if (ch === '@') { state = "q1"; i++; }
      else { return {accepted:false, reason:"q0 esperaba @", steps}; }
    } else if (state === "q1") {
      steps.push(`estado q1, leyendo '${ch}'`);
      if (isLetterDigitOrUnderscore(ch)) { state = "q2"; i++; }
      else { return {accepted:false, reason:"q1 esperaba letra/dígito/_", steps}; }
    } else if (state === "q2") {
      steps.push(`estado q2, leyendo '${ch}'`);
      if (isLetterDigitOrUnderscore(ch)) { i++; }
      else { return {accepted:false, reason:"q2 esperaba letra/dígito/_ o fin", steps}; }
    }
  }
  const accepted = (state === "q2");
  return {accepted, reason: accepted ? "Cadena aceptada" : "Cadena no aceptada", steps};
}

stepBtn.addEventListener("click", () => {
  const s = simInput.value.trim();
  const result = simulateDFA(s);
  simOutput.innerHTML = "<b>Pasos:</b><br>" + result.steps.map(x => "<div>"+x+"</div>").join("") +
                        `<br><b>Resultado:</b> ${result.accepted ? "✅ ACEPTADA" : "❌ RECHAZADA"} - ${result.reason}`;
});

runBtn.addEventListener("click", () => {
  const s = simInput.value.trim();
  const result = simulateDFA(s);
  simOutput.innerHTML = `<b>Resultado final:</b> ${result.accepted ? "✅ ACEPTADA" : "❌ RECHAZADA"} - ${result.reason}`;
});