import {
  Component,
  Input,
  OnChanges,
  SimpleChanges,
  ElementRef,
  ViewChild,
  AfterViewInit,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import * as QRCode from 'qrcode';

@Component({
  selector: 'app-qr-code',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './qr-code.component.html',
})
export class QrCodeComponent implements OnChanges, AfterViewInit {
  @Input() data = '';
  @Input() size = 200;
  @Input() label?: string;
  @ViewChild('qrCanvas') canvasRef!: ElementRef<HTMLCanvasElement>;

  private initialized = false;

  ngAfterViewInit(): void {
    this.initialized = true;
    this.generateQR();
  }

  ngOnChanges(_changes: SimpleChanges): void {
    if (this.initialized) {
      this.generateQR();
    }
  }

  private generateQR(): void {
    if (this.data && this.canvasRef) {
      QRCode.toCanvas(this.canvasRef.nativeElement, this.data, {
        width: this.size,
        margin: 2,
      });
    }
  }
}
