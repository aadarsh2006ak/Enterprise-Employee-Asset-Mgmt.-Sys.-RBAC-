import React, { useEffect, useRef } from 'react';

interface InteractiveGridBackgroundProps {
  cellSize?: number;
  glowRadius?: number;
  className?: string;
}

interface CellTrail {
  col: number;
  row: number;
  alpha: number;
  colorType: number; // For color variation
}

export const InteractiveGridBackground: React.FC<InteractiveGridBackgroundProps> = ({
  cellSize = 38,
  glowRadius = 240,
  className = '',
}) => {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const mouseRef = useRef<{
    x: number;
    y: number;
    targetX: number;
    targetY: number;
    prevX: number;
    prevY: number;
    isInside: boolean;
    speed: number;
  }>({
    x: -1000,
    y: -1000,
    targetX: -1000,
    targetY: -1000,
    prevX: -1000,
    prevY: -1000,
    isInside: false,
    speed: 0,
  });

  const trailsRef = useRef<Map<string, CellTrail>>(new Map());

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext('2d', { alpha: true });
    if (!ctx) return;

    let animationFrameId: number;
    let width = (canvas.width = window.innerWidth);
    let height = (canvas.height = window.innerHeight);

    const handleResize = () => {
      if (!canvas) return;
      width = canvas.width = window.innerWidth;
      height = canvas.height = window.innerHeight;
    };

    window.addEventListener('resize', handleResize);

    const handleMouseMove = (e: MouseEvent) => {
      mouseRef.current.targetX = e.clientX;
      mouseRef.current.targetY = e.clientY;
      mouseRef.current.isInside = true;
    };

    const handleMouseLeave = () => {
      mouseRef.current.isInside = false;
    };

    window.addEventListener('mousemove', handleMouseMove);
    document.addEventListener('mouseleave', handleMouseLeave);

    // Initial default center position
    mouseRef.current.x = width / 2;
    mouseRef.current.y = height / 3;
    mouseRef.current.targetX = width / 2;
    mouseRef.current.targetY = height / 3;

    let lastTime = performance.now();

    const render = (time: number) => {
      const dt = Math.min((time - lastTime) / 1000, 0.1);
      lastTime = time;

      // Smooth mouse follow (LERP)
      const lerpFactor = 0.18;
      const prevX = mouseRef.current.x;
      const prevY = mouseRef.current.y;
      
      mouseRef.current.x += (mouseRef.current.targetX - mouseRef.current.x) * lerpFactor;
      mouseRef.current.y += (mouseRef.current.targetY - mouseRef.current.y) * lerpFactor;

      const dx = mouseRef.current.x - prevX;
      const dy = mouseRef.current.y - prevY;
      mouseRef.current.speed = Math.sqrt(dx * dx + dy * dy);

      const { x: mx, y: my, isInside } = mouseRef.current;

      ctx.clearRect(0, 0, width, height);

      const isDarkMode =
        document.documentElement.classList.contains('dark') ||
        !document.documentElement.classList.contains('light');

      // 1. Draw Static Base Grid Lines (Subtle dark blueprint box pattern)
      ctx.lineWidth = 1;
      ctx.strokeStyle = isDarkMode ? 'rgba(255, 255, 255, 0.045)' : 'rgba(0, 0, 0, 0.055)';

      ctx.beginPath();
      for (let x = 0; x <= width; x += cellSize) {
        ctx.moveTo(x, 0);
        ctx.lineTo(x, height);
      }
      for (let y = 0; y <= height; y += cellSize) {
        ctx.moveTo(0, y);
        ctx.lineTo(width, y);
      }
      ctx.stroke();

      // 2. Add Trail for Current Hovered Cells
      if (isInside || mx >= 0) {
        const hoveredCol = Math.floor(mx / cellSize);
        const hoveredRow = Math.floor(my / cellSize);
        const range = Math.ceil(glowRadius / cellSize);

        for (let r = -range; r <= range; r++) {
          for (let c = -range; c <= range; c++) {
            const col = hoveredCol + c;
            const row = hoveredRow + r;
            const cellCenterX = col * cellSize + cellSize / 2;
            const cellCenterY = row * cellSize + cellSize / 2;
            const dist = Math.hypot(mx - cellCenterX, my - cellCenterY);

            if (dist < glowRadius) {
              const intensity = Math.pow(1 - dist / glowRadius, 1.6);
              const key = `${col},${row}`;
              const existing = trailsRef.current.get(key);
              
              const newAlpha = Math.max(existing ? existing.alpha : 0, intensity);
              const colorType = (Math.abs(col * 7 + row * 13)) % 3; // deterministic pleasant color palette

              trailsRef.current.set(key, {
                col,
                row,
                alpha: newAlpha,
                colorType,
              });
            }
          }
        }
      }

      // 3. Render and Decay Trails
      const decayRate = 1.8 * dt; // Smooth fade-out in ~0.55s
      const entries = Array.from(trailsRef.current.entries());

      for (const [key, cell] of entries) {
        const { col, row, alpha, colorType } = cell;
        const cellX = col * cellSize;
        const cellY = row * cellSize;

        if (cellX + cellSize < 0 || cellX > width || cellY + cellSize < 0 || cellY > height) {
          trailsRef.current.delete(key);
          continue;
        }

        // Color variation palette based on cell:
        // 0: Electric Indigo (rgba(99, 102, 241))
        // 1: Cyber Cyan (rgba(56, 189, 248))
        // 2: Neon Purple (rgba(168, 85, 247))
        let rVal = 99, gVal = 102, bVal = 241;
        if (colorType === 1) {
          rVal = 56; gVal = 189; bVal = 248; // Cyan
        } else if (colorType === 2) {
          rVal = 168; gVal = 85; bVal = 247; // Purple
        }

        // Cell Box Glow Fill
        const fillAlpha = alpha * (isDarkMode ? 0.22 : 0.12);
        ctx.fillStyle = `rgba(${rVal}, ${gVal}, ${bVal}, ${fillAlpha})`;
        ctx.fillRect(cellX + 1, cellY + 1, cellSize - 2, cellSize - 2);

        // Cell Glowing Border
        const borderAlpha = alpha * (isDarkMode ? 0.75 : 0.55);
        ctx.strokeStyle = `rgba(${rVal}, ${gVal}, ${bVal}, ${borderAlpha})`;
        ctx.lineWidth = 1.2;
        ctx.strokeRect(cellX + 0.5, cellY + 0.5, cellSize - 1, cellSize - 1);

        // Decay alpha over time
        cell.alpha -= decayRate;
        if (cell.alpha <= 0.01) {
          trailsRef.current.delete(key);
        }
      }

      // 4. Large Radiant Ambient Cursor Spotlight
      if (mx >= 0 && my >= 0) {
        const spotlightRadius = glowRadius * 1.35;
        const radialGrad = ctx.createRadialGradient(mx, my, 0, mx, my, spotlightRadius);

        if (isDarkMode) {
          radialGrad.addColorStop(0, 'rgba(99, 102, 241, 0.32)');
          radialGrad.addColorStop(0.3, 'rgba(56, 189, 248, 0.16)');
          radialGrad.addColorStop(0.65, 'rgba(139, 92, 246, 0.06)');
          radialGrad.addColorStop(1, 'transparent');
        } else {
          radialGrad.addColorStop(0, 'rgba(99, 102, 241, 0.22)');
          radialGrad.addColorStop(0.35, 'rgba(14, 165, 233, 0.12)');
          radialGrad.addColorStop(0.7, 'rgba(99, 102, 241, 0.04)');
          radialGrad.addColorStop(1, 'transparent');
        }

        ctx.save();
        ctx.fillStyle = radialGrad;
        ctx.beginPath();
        ctx.arc(mx, my, spotlightRadius, 0, Math.PI * 2);
        ctx.fill();
        ctx.restore();

        // 5. High-Tech Grid Intersections (+) Highlight
        const startX = Math.max(0, Math.floor((mx - glowRadius) / cellSize) * cellSize);
        const endX = Math.min(width, Math.ceil((mx + glowRadius) / cellSize) * cellSize);
        const startY = Math.max(0, Math.floor((my - glowRadius) / cellSize) * cellSize);
        const endY = Math.min(height, Math.ceil((my + glowRadius) / cellSize) * cellSize);

        for (let ix = startX; ix <= endX; ix += cellSize) {
          for (let iy = startY; iy <= endY; iy += cellSize) {
            const dist = Math.hypot(mx - ix, my - iy);
            if (dist < glowRadius) {
              const crossIntensity = Math.pow(1 - dist / glowRadius, 1.8);
              const crossAlpha = crossIntensity * (isDarkMode ? 0.9 : 0.7);

              ctx.strokeStyle = `rgba(224, 231, 255, ${crossAlpha})`;
              ctx.lineWidth = 1.2;

              // Draw small cross (+) at intersection
              const arm = 3;
              ctx.beginPath();
              ctx.moveTo(ix - arm, iy);
              ctx.lineTo(ix + arm, iy);
              ctx.moveTo(ix, iy - arm);
              ctx.lineTo(ix, iy + arm);
              ctx.stroke();
            }
          }
        }
      }

      animationFrameId = requestAnimationFrame(render);
    };

    animationFrameId = requestAnimationFrame(render);

    return () => {
      cancelAnimationFrame(animationFrameId);
      window.removeEventListener('resize', handleResize);
      window.removeEventListener('mousemove', handleMouseMove);
      document.removeEventListener('mouseleave', handleMouseLeave);
    };
  }, [cellSize, glowRadius]);

  return (
    <canvas
      ref={canvasRef}
      className={`fixed inset-0 pointer-events-none z-0 transition-opacity duration-500 ${className}`}
      style={{
        willChange: 'transform',
      }}
    />
  );
};

export default InteractiveGridBackground;
