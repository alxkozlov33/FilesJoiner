/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filesjoiner;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

/**
 *
 * @author ibodia
 */
public class MainFrameGUI extends javax.swing.JFrame {
    /**
     * Creates new form MainFrameGUI
     */
    public MainFrameGUI() {
        initComponents();
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnProcessFiles = new javax.swing.JButton();
        lblUrlsCount = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        dropPane1 = new filesjoiner.DropPane();
        lblUrlsCountData = new javax.swing.JLabel();
        cbRemoveDuplicates = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        btnProcessFiles.setText("Process files");
        btnProcessFiles.setMargin(new java.awt.Insets(5, 5, 5, 5));
        btnProcessFiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProcessFilesActionPerformed(evt);
            }
        });

        lblUrlsCount.setText("URLs count:");

        dropPane1.setName(""); // NOI18N

        lblUrlsCountData.setText("0");

        cbRemoveDuplicates.setText("Remove duplicates");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(dropPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(5, 5, 5))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblUrlsCount)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(915, 915, 915)
                                .addComponent(jLabel2)
                                .addContainerGap(13, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lblUrlsCountData)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(cbRemoveDuplicates)
                                .addGap(18, 18, 18)
                                .addComponent(btnProcessFiles)
                                .addGap(5, 5, 5))))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(dropPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(jLabel2)
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnProcessFiles)
                    .addComponent(lblUrlsCount)
                    .addComponent(lblUrlsCountData)
                    .addComponent(cbRemoveDuplicates))
                .addGap(0, 0, 0))
        );

        btnProcessFiles.getAccessibleContext().setAccessibleName("btnProcessFiles");
        lblUrlsCount.getAccessibleContext().setAccessibleName("urlsCountLabel");
        jLabel2.getAccessibleContext().setAccessibleName("urlsCountLabelData");
        dropPane1.getAccessibleContext().setAccessibleName("");

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private void btnProcessFilesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProcessFilesActionPerformed
        // TODO add your handling code here:
        
        System.out.print("Process files action performed");
        LogicSingleton.getLogic().StartRun();
        LogicSingleton.nullLogicObject();
    }//GEN-LAST:event_btnProcessFilesActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainFrameGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainFrameGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainFrameGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainFrameGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainFrameGUI().setVisible(true);
            }
        });
    }
    
    public JLabel getlblUrlsCountData() {
        if (lblUrlsCountData != null) {
            return lblUrlsCountData;
        }
        return null;
    }
    
    public JCheckBox getCbRemoveDuplicates() {
        if (cbRemoveDuplicates != null) {
            return cbRemoveDuplicates;
        }
        return null;
    }
    
    public JButton getBtnProcessFiles() {
        if (btnProcessFiles != null) {
            return btnProcessFiles;
        }
        return null;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnProcessFiles;
    private javax.swing.JCheckBox cbRemoveDuplicates;
    private filesjoiner.DropPane dropPane1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel lblUrlsCount;
    private javax.swing.JLabel lblUrlsCountData;
    // End of variables declaration//GEN-END:variables
}
