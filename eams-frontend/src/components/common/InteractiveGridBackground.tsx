import React, { useEffect, useRef } from 'react';

interface InteractiveGridBackgroundProps {
  cellSize?: number;
  glowRadius?: number;
  className?: string;
}

export const InteractiveGridBackground: React.FC<InteractiveGridBackgroundProps> = ({
  cellSize = 44,
  glowRadius = 260,
  className = '',
}) => {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const mouseRef = useRef<{ x: number; y: number; targetX: number; targetY: number; isInside: boolean }>({
    x: -1000,
    y: -1000,
    targetX: -1000,
    targetY: -1000,
    isInside: false,
  });

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

    // Initial center position for ambient subtle glow
    mouseRef.current.x = width / 2;
    mouseRef.current.y = height / 3;
    mouseRef.current.targetX = width / 2;
    mouseRef.current.targetY = height / 3;

    const render = () => {
      // Smooth mouse follow (LERP)
      const lerpFactor = 0.15;
      mouseRef.current.x += (mouseRef.current.targetX - mouseRef.current.x) * lerpFactor;
      mouseRef.current.y += (mouseRef.current.targetY - mouseRef.current.y) * lerpFactor;

      const { x: mx, y: my, isInside } = mouseRef.current;

      ctx.clearRect(0, 0, width, height);

      const isDarkMode =
        document.documentElement.classList.contains('dark') ||
        !document.documentElement.classList.contains('light');

      // Grid Colors
      const baseLineColor = isDarkMode ? 'rgba(255, 255, 255, 0.038)' : 'rgba(0, 0, 0, 0.05)';

      // 1. Draw static subtle base grid lines
      ctx.lineWidth = 1;
      ctx.strokeStyle = baseLineColor;

      ctx.beginPath();
      // Vertical lines
      for (let x = 0; x <= width; x += cellSize) {
        ctx.moveTo(x, 0);
        ctx.lineTo(x, height);
      }
      // Horizontal lines
      for (let y = 0; y <= height; y += cellSize) {
        ctx.moveTo(0, y);
        ctx.lineTo(width, y);
      }
      ctx.stroke();

      // 2. Identify hovered grid cells and illuminate them
      if (isInside || mx >= 0) {
        const hoveredCol = Math.floor(mx / cellSize);
        const hoveredRow = Math.floor(my / cellSize);

        // Highlight nearby cells
        const range = Math.ceil(glowRadius / cellSize);
        for (let r = -range; r <= range; r++) {
          for (let c = -range; c <= range; c++) {
            const cellCol = hoveredCol + c;
            const cellRow = hoveredRow + r;
            const cellX = cellCol * cellSize;
            const cellY = cellRow * cellSize;

            // Distance from mouse to cell center
            const centerX = cellX + cellSize / 2;
            const centerY = cellY + cellSize / 2;
            const dist = Math.hypot(mx - centerX, my - centerY);

            if (dist < glowRadius) {
              const intensity = Math.pow(1 - dist / glowRadius, 1.8);

              // Draw illuminated box fill
              const fillAlpha = intensity * (isDarkMode ? 0.14 : 0.08);
              ctx.fillStyle = isDarkMode
                ? `rgba(99, 102, 241, ${fillAlpha})`
                : `rgba(79, 70, 229, ${fillAlpha})`;
              ctx.fillRect(cellX + 1, cellY + 1, cellSize - 2, cellSize - 2);

              // Draw highlighted cell borders
              const borderAlpha = intensity * (isDarkMode ? 0.55 : 0.4);
              ctx.strokeStyle = isDarkMode
                ? `rgba(129, 140, 248, ${borderAlpha})`
                : `rgba(99, 102, 241, ${borderAlpha})`;
              ctx.lineWidth = 1.2;
              ctx.strokeRect(cellX + 0.5, cellY + 0.5, cellSize - 1, cellSize - 1);
            }
          }
        }
      }

      // 3. Draw Radial Spotlight Glow on Grid Lines around Cursor
      if (mx >= 0 && my >= 0) {
        const gradient = ctx.createRadialGradient(mx, my, 0, mx, my, glowRadius * 1.2);
        if (isDarkMode) {
          gradient.addColorStop(0, 'rgba(99, 102, 241, 0.32)');
          gradient.addColorStop(0.35, 'rgba(139, 92, 246, 0.16)');
          gradient.addColorStop(0.7, 'rgba(56, 189, 248, 0.05)');
          gradient.addColorStop(1, 'transparent');
        } else {
          gradient.addColorStop(0, 'rgba(99, 102, 241, 0.2)');
          gradient.addColorStop(0.4, 'rgba(79, 70, 229, 0.1)');
          gradient.addColorStop(0.8, 'rgba(14, 165, 233, 0.03)');
          gradient.addColorStop(1, 'transparent');
        }

        ctx.save();
        ctx.fillStyle = gradient;
        ctx.beginPath();
        ctx.arc(mx, my, glowRadius * 1.2, 0, Math.PI * 2);
        ctx.fill();
        ctx.restore();

        // 4. Draw glowing intersection dots / pluses near cursor
        const startX = Math.max(0, Math.floor((mx - glowRadius) / cellSize) * cellSize);
        const endX = Math.min(width, Math.ceil((mx + glowRadius) / cellSize) * cellSize);
        const startY = Math.max(0, Math.floor((my - glowRadius) / cellSize) * cellSize);
        const endY = Math.min(height, Math.ceil((my + glowRadius) / cellSize) * cellSize);

        for (let ix = startX; ix <= endX; ix += cellSize) {
          for (let iy = startY; iy <= endY; iy += cellSize) {
            const dist = Math.hypot(mx - ix, my - iy);
            if (dist < glowRadius) {
              const intensity = Math.pow(1 - dist / glowRadius, 1.5);
              const dotAlpha = intensity * (isDarkMode ? 0.9 : 0.7);

              ctx.fillStyle = isDarkMode
                ? `rgba(165, 180, 252, ${dotAlpha})`
                : `rgba(99, 102, 241, ${dotAlpha})`;

              // Glowing dot at intersection
              ctx.beginPath();
              ctx.arc(ix, iy, 1.5 + intensity * 1.5, 0, Math.PI * 2);
              ctx.fill();
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
