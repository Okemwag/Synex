import fs from "node:fs";
import path from "node:path";

const root = path.resolve(import.meta.dirname, "..");
const contract = JSON.parse(fs.readFileSync(path.join(root, "contract/synex-client-contract.json"), "utf8"));
const bindings = JSON.parse(fs.readFileSync(path.join(root, "contract/client-bindings.json"), "utf8"));

for (const binding of bindings.bindings) {
  const expected = contract.types[binding.contract];
  if (!expected) throw new Error(`Unknown backend contract ${binding.contract}`);
  const source = fs.readFileSync(path.join(root, binding.source), "utf8");
  const declaration = extractBalanced(source, `data class ${binding.type}`, "(", ")");
  for (const wireField of expected) {
    const property = binding.fields?.[wireField] || wireField;
    if (!new RegExp(`\\bval\\s+${escapeRegExp(property)}\\s*:`).test(declaration)) {
      throw new Error(`${binding.type} no longer consumes ${wireField} as ${property}`);
    }
  }
}

console.log(`Kotlin DTOs match Synex client contract v${contract.version}`);

function extractBalanced(source, marker, open, close) {
  const markerIndex = source.indexOf(marker);
  if (markerIndex < 0) throw new Error(`Missing declaration ${marker}`);
  const start = source.indexOf(open, markerIndex);
  if (start < 0) throw new Error(`Malformed declaration ${marker}`);
  let depth = 0;
  for (let index = start; index < source.length; index += 1) {
    if (source[index] === open) depth += 1;
    if (source[index] === close && --depth === 0) return source.slice(start, index + 1);
  }
  throw new Error(`Unclosed declaration ${marker}`);
}

function escapeRegExp(value) { return value.replace(/[.*+?^${}()|[\]\\]/g, "\\$&"); }
